/*
 * Copyright (C) 2018-2024 Alexander Schmid
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.alexqp.redye.main;

import com.github.alexqp.commons.bstats.bukkit.Metrics;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.redye.listeners.CauldronItemDropListener;
import com.google.common.collect.Range;
import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.Debugable;
import com.jeff_media.updatechecker.UpdateCheckSource;
import com.jeff_media.updatechecker.UpdateChecker;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.alexqp.redye.listeners.RecipeDiscoverConnectionListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Redye extends JavaPlugin implements Debugable {

    // TODO remove VANILLA wool, carpet and bed recipes if disabled in config (1.20 onwards). Remove note in config then.

    @Override
    public boolean getDebug() {
        return false;
    }

    private static final String defaultInternalsVersion = "v1_20_R1";
    private static InternalsProvider internals;
    static {
        try {
            String packageName = Redye.class.getPackage().getName();
            String internalsName = getInternalsName(Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
            if (internalsName.equals(defaultInternalsVersion)) {
                Bukkit.getLogger().log(Level.INFO, "Redye is using the latest implementation (last tested for " + defaultInternalsVersion + ").");
                internals = new InternalsProvider();
            } else {
                Bukkit.getLogger().log(Level.INFO, "Redye is using the implementation for version " + internalsName + ".");
                internals = (InternalsProvider) Class.forName(packageName + "." + internalsName).getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException exception) {
            Bukkit.getLogger().log(Level.WARNING, "Redye could not find an updated implementation for this server version. " +
                    "However the plugin is trying to use the latest implementation which should work if Minecraft did not change drastically (last tested version: " + defaultInternalsVersion + ").");
            internals = new InternalsProvider();
        }
    }

    private static String getInternalsName(String internalsName) {
        Map<String, String> internalsVersions = new HashMap<>();
        internalsVersions.put("v1_13_R1", "v1_13_R2");
        internalsVersions.put("v1_13_R2", "v1_13_R2");

        internalsVersions.put("v1_14_R1", "v1_16_R3");
        internalsVersions.put("v1_15_R1", "v1_16_R3");
        internalsVersions.put("v1_16_R1", "v1_16_R3");
        internalsVersions.put("v1_16_R2", "v1_16_R3");
        internalsVersions.put("v1_16_R3", "v1_16_R3");

        return internalsVersions.getOrDefault(internalsName, defaultInternalsVersion);
    }

    private final HashSet<RedyeMaterial> redyeMats = internals.getDefaultRedyeMaterials();
    private final String[] recipeBookSectionConfigNames = {"recipe_book_options",
            "add_recipes_on_login", "remove_recipes_on_logout", "group_recipes_with_vanilla"};
    @Override
    public void onEnable() {
        new Metrics(this, 3023);
        this.saveDefaultConfig();
        this.getLogger().info("This plugin was made by alex_qp");
        this.updateChecker();

        ConfigChecker configChecker = new ConfigChecker(this);

        // ------------------------------------------------------------------
        // RECIPE BOOK OPTIONS
        // ------------------------------------------------------------------

        Boolean[] recipeBookConnection = {false, false};
        boolean recipeGroupVanilla = true;
        ConfigurationSection section = configChecker.checkConfigSection(this.getConfig(), recipeBookSectionConfigNames[0], ConsoleErrorType.ERROR);
        if (section != null) {
            recipeBookConnection[0] = configChecker.checkBoolean(section, recipeBookSectionConfigNames[1], ConsoleErrorType.WARN, recipeBookConnection[0]);
            recipeBookConnection[1] = configChecker.checkBoolean(section, recipeBookSectionConfigNames[2], ConsoleErrorType.WARN, recipeBookConnection[1]);
            recipeGroupVanilla = configChecker.checkBoolean(section, recipeBookSectionConfigNames[3], ConsoleErrorType.WARN, true);
        }

        // ------------------------------------------------------------------
        // DYE RECIPES
        // ------------------------------------------------------------------

        HashSet<HashSet<NamespacedKey>> allKeys = new HashSet<>(this.checkColorRecipes(configChecker, recipeGroupVanilla));

        // ------------------------------------------------------------------
        // UNDYE RECIPES / CAULDRON
        // ------------------------------------------------------------------

        ConfigurationSection undyeRootSection = configChecker.checkConfigSection(this.getConfig(), "bleaching", ConsoleErrorType.ERROR);
        if (undyeRootSection != null) {
            allKeys.addAll(this.checkUndyeRecipes(configChecker, undyeRootSection, recipeGroupVanilla));

            CauldronItemDropListener cauldronItemDropListener = CauldronItemDropListener.build(this, internals, undyeRootSection);
            if (cauldronItemDropListener != null) {
                Bukkit.getPluginManager().registerEvents(cauldronItemDropListener, this);
                this.getLogger().info("enabled cauldron bleaching for at least one item");
            }
        }

        // ------------------------------------------------------------------

        if (recipeBookConnection[0]) {
            Bukkit.getServer().getPluginManager().registerEvents(new RecipeDiscoverConnectionListener(this, allKeys, recipeBookConnection[1]), this);
            ConsoleMessage.debug((Debugable) this, "registered RecipeDiscoverJoinListener");
        }
    }

    private Set<HashSet<NamespacedKey>> checkColorRecipes(@NotNull ConfigChecker configChecker, boolean recipeGroupVanilla) {
        HashSet<HashSet<NamespacedKey>> addedKeys = new HashSet<>();
        ConfigurationSection section = configChecker.checkConfigSection(this.getConfig(), "color_recipes", ConsoleErrorType.ERROR);
        for (RedyeMaterial redyeMat : this.getEnabledRedyeMaterials(configChecker, section, 8)) {
            if (!recipeGroupVanilla) {
                redyeMat.setVanillaGroupName("redye_" + redyeMat.getVanillaGroupName());
            }
            addedKeys.add(internals.addColorRecipes(this, redyeMat));
            ConsoleMessage.debug((Debugable) this, "added color recipes for " + redyeMat.getConfigName());
            this.getLogger().info("added color recipes for " + redyeMat.getConfigName() + " with amount " + redyeMat.getInput());
        }
        return addedKeys;
    }

    private Set<HashSet<NamespacedKey>> checkUndyeRecipes(@NotNull ConfigChecker configChecker, @NotNull ConfigurationSection undyeRootSection, boolean recipeGroupVanilla) {
        HashSet<HashSet<NamespacedKey>> addedKeys = new HashSet<>();

        ConfigurationSection undyeRecipeSection = configChecker.checkConfigSection(undyeRootSection, "recipes", ConsoleErrorType.ERROR);
        if (undyeRecipeSection == null)
            return addedKeys;

        String neutralMaterialConfigName = "neutral_material";
        String matName = configChecker.checkString(undyeRecipeSection, neutralMaterialConfigName, ConsoleErrorType.WARN, "ICE");
        assert matName != null;

        Material neutralDyeMat = Material.matchMaterial(matName);
        if (neutralDyeMat == null) {
            ConsoleMessage.send(ConsoleErrorType.WARN, this, neutralMaterialConfigName + "was not a valid material name. Used ICE instead.");
            neutralDyeMat = Material.ICE;
        }

        ConfigurationSection section = configChecker.checkConfigSection(undyeRecipeSection, "enable", ConsoleErrorType.ERROR);
        for (RedyeMaterial redyeMat : this.getEnabledRedyeMaterials(configChecker, section, 8)) {
            String undyeMatName;
            if (redyeMat.hasUndyeMatName())
                undyeMatName = redyeMat.getUndyeMatName();
            else
                undyeMatName = "WHITE_" + redyeMat.getColorMatName();
            if (!recipeGroupVanilla) {
                redyeMat.setVanillaGroupName("redye_" + redyeMat.getVanillaGroupName());
            }
            addedKeys.add(internals.addUndyeRecipes(this, redyeMat.getColorMatName(), neutralDyeMat, Material.valueOf(undyeMatName), redyeMat.getInput(), redyeMat.getVanillaGroupName()));
            this.getLogger().info("added undye recipes for " + redyeMat.getConfigName() + " with amount " + redyeMat.getInput());
        }
        return addedKeys;
    }

    @NotNull
    public Set<RedyeMaterial> getEnabledRedyeMaterials(@NotNull ConfigChecker configChecker, @Nullable ConfigurationSection section, int maxInput) {
        HashSet<RedyeMaterial> enabledMaterials = new HashSet<>();
        if (section != null) {
            for (RedyeMaterial redyeMat : new HashSet<>(internals.getDefaultRedyeMaterials())) {
                redyeMat.setInput(configChecker.checkInt(section, redyeMat.getConfigName(), ConsoleErrorType.WARN, redyeMat.getInput(), Range.closed(0, maxInput)));
                if (redyeMat.getInput() > 0)
                    enabledMaterials.add(redyeMat);
            }
        }
        return enabledMaterials;
    }

    private void updateChecker() {
        int spigotResourceID = 59446;
        ConfigChecker configChecker = new ConfigChecker(this);
        ConfigurationSection updateCheckerSection = configChecker.checkConfigSection(this.getConfig(), "updatechecker", ConsoleErrorType.ERROR);
        if (updateCheckerSection != null && configChecker.checkBoolean(updateCheckerSection, "enable", ConsoleErrorType.WARN, true)) {
            ConsoleMessage.debug((Debugable) this, "enabled UpdateChecker");

            new UpdateChecker(this, UpdateCheckSource.SPIGOT, String.valueOf(spigotResourceID))
                    .setDownloadLink(spigotResourceID)
                    .setChangelogLink("https://www.spigotmc.org/resources/" + spigotResourceID + "/updates")
                    .setDonationLink("https://paypal.me/alexqpplugins")
                    .setNotifyOpsOnJoin(configChecker.checkBoolean(updateCheckerSection, "notify_op_on_login", ConsoleErrorType.WARN, true))
                    .setNotifyByPermissionOnJoin("redye.updatechecker")
                    .checkEveryXHours(24).checkNow();
        }
    }
}
