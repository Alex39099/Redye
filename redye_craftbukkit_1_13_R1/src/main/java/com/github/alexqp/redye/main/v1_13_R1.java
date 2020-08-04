package com.github.alexqp.redye.main;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.HashSet;

public class v1_13_R1 extends InternalsProvider {

    @Override
    protected void createColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("BLACK_", Material.INK_SAC);
        colorMap.put("BLUE_", Material.LAPIS_LAZULI);
        colorMap.put("BROWN_", Material.COCOA_BEANS);
        colorMap.put("CYAN_", Material.CYAN_DYE);
        colorMap.put("GREEN_", Material.CACTUS_GREEN);
        colorMap.put("LIGHT_BLUE_", Material.LIGHT_BLUE_DYE);
        colorMap.put("LIGHT_GRAY_", Material.LIGHT_GRAY_DYE);
        colorMap.put("GRAY_", Material.GRAY_DYE);
        colorMap.put("LIME_", Material.LIME_DYE);
        colorMap.put("MAGENTA_", Material.MAGENTA_DYE);
        colorMap.put("ORANGE_", Material.ORANGE_DYE);
        colorMap.put("PINK_", Material.PINK_DYE);
        colorMap.put("PURPLE_", Material.PURPLE_DYE);
        colorMap.put("RED_", Material.ROSE_RED);
        colorMap.put("WHITE_", Material.BONE_MEAL);
        colorMap.put("YELLOW_", Material.DANDELION_YELLOW);
    }

    @Override
    protected void createRedyeMaterials() {
        redyeMats = new HashSet<>();
        redyeMats.add(new RedyeMaterial("terracotta", "TERRACOTTA", "TERRACOTTA", 8, "stained_terracotta"));
        redyeMats.add(new RedyeMaterial("glazed_terracotta", "GLAZED_TERRACOTTA", 1, "glazed_terracotta"));
        redyeMats.add(new RedyeMaterial("glass", "STAINED_GLASS", "GLASS", 8, "stained_glass"));
        redyeMats.add(new RedyeMaterial("glass_pane", "STAINED_GLASS_PANE", "GLASS_PANE", 8, "stained_glass_pane", true));
        redyeMats.add(new RedyeMaterial("concrete", "CONCRETE", 8, "concrete")); // not really vanillaGroupName!
        redyeMats.add(new RedyeMaterial("concrete_powder", "CONCRETE_POWDER", 8, "concrete_powder"));
        redyeMats.add(new RedyeMaterial("wool", "WOOL", 1, "wool"));
        redyeMats.add(new RedyeMaterial("carpet", "CARPET", 8, "carpet"));
    }
}
