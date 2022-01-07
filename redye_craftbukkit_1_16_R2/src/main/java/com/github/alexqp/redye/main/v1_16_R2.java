/*
 * Copyright (C) 2018-2022 Alexander Schmid
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

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;

public class v1_16_R2 extends InternalsProvider {

    @Override
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

    @Override
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
    }

    @Override
    public Material getWaterCauldron() {
        return Material.CAULDRON;
    }

    @Override
    public void emptyCauldron(@NotNull Block cauldron) {
        BlockData cauldronData = cauldron.getBlockData();
        if (cauldronData instanceof Levelled) {
            ((Levelled) cauldronData).setLevel(0);
            cauldron.setBlockData(cauldronData);
        }
    }
}
