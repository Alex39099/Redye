/*
 * Copyright (C) 2018-2021 Alexander Schmid
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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.java.JavaPlugin;
import com.github.alexqp.redye.listeners.RecipeDiscoverConnectionListener;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class Redye extends JavaPlugin implements Debugable {

    @Override
    public boolean getDebug() {
        return false;
    }

    private static InternalsProvider internals;
    static {
        try {
            String packageName = Redye.class.getPackage().getName();
            String internalsName = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            internals = (InternalsProvider) Class.forName(packageName + "." + internalsName).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ClassCastException | NoSuchMethodException | InvocationTargetException exception) {
            Bukkit.getLogger().log(Level.SEVERE, "Redye could not find a valid implementation for this server version.");
            internals = new InternalsProvider();
        }
    }

    private final HashSet<RedyeMaterial> redyeMats = internals.getRedyeMaterials();
    private final String[] recipeBookSectionConfigNames = {"recipe_book_options",
            "add_recipes_on_login", "remove_recipes_on_logout", "group_recipes_with_vanilla"};

    @Override
    public void onEnable() {
        new Metrics(this);
        this.saveDefaultConfig();
        this.getLogger().info("This plugin was made by alex_qp");

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

        HashSet<HashSet<NamespacedKey>> allKeys = new HashSet<>();

        section = configChecker.checkConfigSection(this.getConfig(), "enabled_recipes", ConsoleErrorType.ERROR);
        if (section != null) {

            for (RedyeMaterial redyeMat : new HashSet<>(redyeMats)) {
                redyeMat.setInput(configChecker.checkInt(section, redyeMat.getConfigName(), ConsoleErrorType.WARN, redyeMat.getInput(), Range.closed(0, 8)));

                if (!recipeGroupVanilla) {
                    redyeMat.setVanillaGroupName("redye_" + redyeMat.getVanillaGroupName());
                }

                if (redyeMat.getInput() < 1)
                    continue;

                allKeys.add(internals.addColorRecipes(this, redyeMat));
                ConsoleMessage.debug((Debugable) this, "added color recipes for " + redyeMat.getConfigName());

                this.getLogger().info("added recipes for " + redyeMat.getConfigName() + " for amount " + redyeMat.getInput());
            }
        }

        // ------------------------------------------------------------------
        // UNDYE RECIPES / CAULDRON
        // ------------------------------------------------------------------

        ConfigurationSection undyeRootSection = configChecker.checkConfigSection(this.getConfig(), "undye", ConsoleErrorType.ERROR);
        if (undyeRootSection != null) {

            section = configChecker.checkConfigSection(undyeRootSection, "recipes", ConsoleErrorType.ERROR);
            if (section != null) {

                String neutralMaterialConfigName = "neutral_material";
                String matName = configChecker.checkString(section, neutralMaterialConfigName, ConsoleErrorType.WARN, "ICE");
                assert matName != null;

                Material neutralDyeMat = Material.matchMaterial(matName);
                if (neutralDyeMat == null) {
                    ConsoleMessage.send(ConsoleErrorType.WARN, this, neutralMaterialConfigName + "was not a valid material name. Used ICE instead.");
                    neutralDyeMat = Material.ICE;
                }

                section = configChecker.checkConfigSection(section, "enable", ConsoleErrorType.ERROR);
                if (section != null) {

                    for (RedyeMaterial redyeMat : internals.getRedyeMaterials()) {

                        if (configChecker.checkBoolean(section, redyeMat.getConfigName(), ConsoleErrorType.WARN, false)) {
                            String undyeMatName;
                            if (redyeMat.hasUndyeMatName())
                                undyeMatName = redyeMat.getUndyeMatName();
                            else
                                undyeMatName = "WHITE_" + redyeMat.getColorMatName();
                            allKeys.add(internals.addUndyeRecipes(this, redyeMat.getColorMatName(), neutralDyeMat, Material.valueOf(undyeMatName), redyeMat.getInput(), redyeMat.getVanillaGroupName()));
                            ConsoleMessage.debug((Debugable) this, "added undyeMat recipes for " + redyeMat.getConfigName());
                        }
                    }
                }
            }

            CauldronItemDropListener cauldronItemDropListener = CauldronItemDropListener.build(this, internals, undyeRootSection);
            if (cauldronItemDropListener != null) {
                Bukkit.getPluginManager().registerEvents(cauldronItemDropListener, this);
                ConsoleMessage.debug((Debugable) this, "registered ItemThrowListener");
            }
        }


        if (recipeBookConnection[0]) {
            Bukkit.getServer().getPluginManager().registerEvents(new RecipeDiscoverConnectionListener(this, allKeys, recipeBookConnection[1]), this);
            ConsoleMessage.debug((Debugable) this, "registered RecipeDiscoverJoinListener");
        }
    }
}
