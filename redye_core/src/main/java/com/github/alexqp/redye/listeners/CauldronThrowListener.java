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

package com.github.alexqp.redye.listeners;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.redye.main.InternalsProvider;
import com.github.alexqp.redye.main.RedyeMaterial;
import com.google.common.collect.Range;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class CauldronThrowListener implements Listener {

    @Nullable
    public static CauldronThrowListener build(JavaPlugin plugin, InternalsProvider internals, ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);
        rootSection = configChecker.checkConfigSection(rootSection, "cauldron", ConsoleErrorType.ERROR);
        if (rootSection != null) {
            ConfigurationSection section = configChecker.checkConfigSection(rootSection, "enable", ConsoleErrorType.ERROR);
            if (section != null) {

                Set<RedyeMaterial> enabledMaterials = new HashSet<>();
                for (RedyeMaterial redyeMat : internals.getRedyeMaterials()) {

                    if (redyeMat.hasUndyeMatName() && configChecker.checkBoolean(section, redyeMat.getConfigName(), ConsoleErrorType.WARN, true)) {
                        enabledMaterials.add(redyeMat);
                        ConsoleMessage.debug(CauldronThrowListener.class, plugin, "prepared redyeMat " + redyeMat.getConfigName() + " to enable cauldron mechanic");
                    }
                }

                int checkEmpty = configChecker.checkInt(rootSection, "check_empty", ConsoleErrorType.WARN, 1, Range.closed(0, 2));
                int changeWater = configChecker.checkInt(rootSection, "change_waterlevel", ConsoleErrorType.WARN, 1, Range.closed(0, 3));

                return new CauldronThrowListener(plugin, enabledMaterials, checkEmpty, changeWater);
            }
        }
        return null;
    }

    private final JavaPlugin plugin;
    private final Set<RedyeMaterial> enabledMaterials;
    private final int checkEmpty;
    private final int changeWater;

    private CauldronThrowListener(JavaPlugin plugin, Set<RedyeMaterial> enabledMaterials, int checkEmpty, int changeWater) {
        this.plugin = plugin;
        this.enabledMaterials = enabledMaterials;
        this.checkEmpty = checkEmpty;
        this.changeWater = changeWater;
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        String dropItemType = e.getItemDrop().getItemStack().getType().name();

        for (RedyeMaterial redyeMaterial : enabledMaterials) {

            if (dropItemType.endsWith(redyeMaterial.getColorMatName())) {

                ItemStack undyedItem = new ItemStack(e.getItemDrop().getItemStack());
                if (redyeMaterial.hasUndyeMatName()) {
                    undyedItem.setType(Material.valueOf(redyeMaterial.getUndyeMatName()));
                } else {
                    ConsoleMessage.debug(this.getClass(), plugin, "The RedyeMaterial " + redyeMaterial.getConfigName() + " has no undyeMaterialName for " + dropItemType);
                    break;
                }

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Block cauldron = e.getItemDrop().getLocation().getBlock();
                        if (cauldron.getBlockData().getMaterial().equals(Material.CAULDRON)) {
                            Levelled cauldronData = (Levelled) cauldron.getBlockData();

                            int neededWater;
                            if (checkEmpty == 2)
                                neededWater = changeWater;
                            else
                                neededWater = checkEmpty;

                            int oldWater = cauldronData.getLevel();
                            if (neededWater > oldWater) {
                                ConsoleMessage.debug(this.getClass(), plugin, "neededWater = " + neededWater + " was greater than oldWater = " + oldWater + " (checkEmpty = " + checkEmpty);
                                return;
                            }

                            int newWater = Math.max(0, oldWater - changeWater);

                            cauldronData.setLevel(newWater);
                            cauldron.setBlockData(cauldronData);

                            e.getItemDrop().setItemStack(undyedItem);
                        }
                    }
                }.runTaskLater(plugin, 20);

                break;
            }
        }

    }
}
