/*
 * Copyright (C) 2018-2023 Alexander Schmid
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

import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class InternalsProvider {

    protected Map<String, Material> colorMap;
    protected HashSet<RedyeMaterial> redyeMats;

    protected InternalsProvider() {
        this.createColorMap();
        this.createRedyeMaterials();
    }

    // this should get overwritten by version implementation
    protected void createColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("BLACK_", Material.BLACK_DYE);
        colorMap.put("BLUE_", Material.BLUE_DYE);
        colorMap.put("BROWN_", Material.BROWN_DYE);
        colorMap.put("CYAN_", Material.CYAN_DYE);
        colorMap.put("GREEN_", Material.GREEN_DYE);
        colorMap.put("LIGHT_BLUE_", Material.LIGHT_BLUE_DYE);
        colorMap.put("LIGHT_GRAY_", Material.LIGHT_GRAY_DYE);
        colorMap.put("GRAY_", Material.GRAY_DYE);
        colorMap.put("LIME_", Material.LIME_DYE);
        colorMap.put("MAGENTA_", Material.MAGENTA_DYE);
        colorMap.put("ORANGE_", Material.ORANGE_DYE);
        colorMap.put("PINK_", Material.PINK_DYE);
        colorMap.put("PURPLE_", Material.PURPLE_DYE);
        colorMap.put("RED_", Material.RED_DYE);
        colorMap.put("WHITE_", Material.WHITE_DYE);
        colorMap.put("YELLOW_", Material.YELLOW_DYE);
    }

    // this should get overwritten by version implementation
    protected void createRedyeMaterials() {
        redyeMats = new HashSet<>();
        redyeMats.add(new RedyeMaterial("terracotta", "TERRACOTTA", "TERRACOTTA", 8, "stained_terracotta"));
        redyeMats.add(new RedyeMaterial("glazed_terracotta", "GLAZED_TERRACOTTA", 1, "glazed_terracotta"));
        redyeMats.add(new RedyeMaterial("glass", "STAINED_GLASS", "GLASS", 8, "stained_glass"));
        redyeMats.add(new RedyeMaterial("glass_pane", "STAINED_GLASS_PANE", "GLASS_PANE", 8, "stained_glass_pane"));
        redyeMats.add(new RedyeMaterial("concrete", "CONCRETE", 8, "concrete")); // not really vanillaGroupName!
        redyeMats.add(new RedyeMaterial("concrete_powder", "CONCRETE_POWDER", 8, "concrete_powder"));
        redyeMats.add(new RedyeMaterial("wool", "WOOL", 1, "wool"));
        redyeMats.add(new RedyeMaterial("carpet", "CARPET", 8, "carpet"));
        redyeMats.add(new RedyeMaterial("banner", "BANNER", 1, "banner")); // not really vanillaGroupName!
        redyeMats.add(new RedyeMaterial("candle", "CANDLE", "CANDLE", 1, "candle"));
    }

    Map<String, Material> getColorMap() {
        return new HashMap<>(this.colorMap);
    }

    /**
     * Get a deep copy of the default redye materials.
     * @return a deep copy of the default redye materials
     */
    @NotNull
    public HashSet<RedyeMaterial> getDefaultRedyeMaterials() {
        HashSet<RedyeMaterial> materials = new HashSet<>();
        for (RedyeMaterial redyeMaterial : redyeMats) {
            materials.add(redyeMaterial.copy());
        }
        return materials;
    }

    private @NotNull List<Material> getColorMaterials(@NotNull String matName) {
        List<Material> materials = new ArrayList<>();
        for (String colorPrefix : this.getColorMap().keySet()) {
            Material mat = Material.matchMaterial(colorPrefix + matName);
            if (mat != null) {
                materials.add(mat);
            }
        }
        return materials;
    }

    /**
     * Adds for every color a recipe to craft color_matName (result) out of any !other! color_matName (ingredient) + colorDye
     * @param plugin the plugin
     * @param redyeMat the redyeMat
     * @return a set of added NamespacedKeys
     */
    HashSet<NamespacedKey> addColorRecipes(JavaPlugin plugin, RedyeMaterial redyeMat) {
        HashSet<NamespacedKey> keys = new HashSet<>();

        String matName = redyeMat.getColorMatName();
        int input = redyeMat.getInput();
        String groupName = redyeMat.getVanillaGroupName();

        List<Material> inputMaterials = this.getColorMaterials(matName);

        if (redyeMat.isUndyeable()) {
            inputMaterials.add(Material.valueOf(redyeMat.getUndyeMatName()));
            ConsoleMessage.debug(plugin, "adding undyeableToColor recipes for " + redyeMat.getConfigName());
        }

        for (String resColor : this.getColorMap().keySet()) {
            String coloredResMatName = resColor + matName;
            Material resMat = Material.matchMaterial(coloredResMatName);
            if (resMat == null) {
                ConsoleMessage.debug(this.getClass(), plugin, coloredResMatName + " was no valid material, skipped.");
                continue;
            }

            NamespacedKey key = new NamespacedKey(plugin, coloredResMatName); // + input
            RecipeChoice recipeChoice = new RecipeChoice.MaterialChoice(inputMaterials);
            ItemStack result = new ItemStack(resMat, input);

            ShapelessRecipe recipe = new ShapelessRecipe(key, result);
            recipe.addIngredient(this.getColorMap().get(resColor));
            for (int i = 0; i < input; i++) {
                recipe.addIngredient(recipeChoice);
            }
            recipe.setGroup(groupName);

            Bukkit.addRecipe(recipe);
            keys.add(key);
        }
        return keys;
    }


    /**
     * Add a recipe to craft resultMat out of any color_matName (ingredient) + neutralDyeMat
     * @param plugin the plugin
     * @param matName the ingredients mat name (gets colored)
     * @param neutralDyeMat the "neutral-dye" (i. e. ICE)
     * @param resultMat the result material
     * @param input the amount of ingredients / result
     * @param groupName the recipe group name (sorts in recipe book)
     * @return the namespacedKey of the added recipe.
     */
    HashSet<NamespacedKey> addUndyeRecipes(JavaPlugin plugin, String matName, Material neutralDyeMat, Material resultMat, int input, String groupName) {
        HashSet<NamespacedKey> keys = new HashSet<>();

        NamespacedKey key = new NamespacedKey(plugin, "NEUTRAL_" + resultMat.name());
        ItemStack result = new ItemStack(resultMat, input);

        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        recipe.addIngredient(neutralDyeMat); // dye
        for (int i = 0; i < input; i++) {
            recipe.addIngredient(new RecipeChoice.MaterialChoice(this.getColorMaterials(matName)));
        }
        recipe.setGroup(groupName);

        Bukkit.addRecipe(recipe);
        keys.add(key);
        return keys;
    }

    // this should get overwritten by version implementation
    public Material getWaterCauldron() {
        return Material.WATER_CAULDRON;
    }

    /**
     * Empties a cauldron block.
     * <p>Note: Action may take place even tho block is no cauldron.</p>
     * @param cauldron cauldron block.
     */
    public void emptyCauldron(@NotNull Block cauldron) {
        cauldron.setType(Material.CAULDRON);
    }
}
