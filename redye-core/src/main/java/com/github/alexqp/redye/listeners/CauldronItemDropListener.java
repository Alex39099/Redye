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

package com.github.alexqp.redye.listeners;

import com.github.alexqp.commons.config.ConfigChecker;
import com.github.alexqp.commons.config.ConsoleErrorType;
import com.github.alexqp.commons.messages.ConsoleMessage;
import com.github.alexqp.redye.main.InternalsProvider;
import com.github.alexqp.redye.main.Redye;
import com.github.alexqp.redye.main.RedyeMaterial;
import com.google.common.collect.Range;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemMergeEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

public class CauldronItemDropListener implements Listener {

    @Nullable
    public static CauldronItemDropListener build(Redye plugin, InternalsProvider internals, ConfigurationSection rootSection) {
        ConfigChecker configChecker = new ConfigChecker(plugin);
        rootSection = configChecker.checkConfigSection(rootSection, "cauldron", ConsoleErrorType.ERROR);
        if (rootSection != null) {
            ConfigurationSection section = configChecker.checkConfigSection(rootSection, "enable", ConsoleErrorType.ERROR);
            if (section != null) {

                Set<RedyeMaterial> enabledMaterials = plugin.getEnabledRedyeMaterials(configChecker, section, 64);

                int checkEmpty = configChecker.checkInt(rootSection, "check_empty", ConsoleErrorType.WARN, 1, Range.closed(0, 2));
                int changeWater = configChecker.checkInt(rootSection, "change_waterlevel", ConsoleErrorType.WARN, 1, Range.closed(0, 3));

                return new CauldronItemDropListener(plugin, internals, enabledMaterials, checkEmpty, changeWater);
            }
        }
        return null;
    }

    private final JavaPlugin plugin;
    private final InternalsProvider internals;
    private final Set<RedyeMaterial> enabledMaterials;
    private final int checkEmpty;
    private final int changeWater;

    private final HashMap<Item, BukkitRunnable> cauldronDrops = new HashMap<>();

    private CauldronItemDropListener(JavaPlugin plugin, InternalsProvider internals, Set<RedyeMaterial> enabledMaterials, int checkEmpty, int changeWater) {
        this.plugin = plugin;
        this.internals = internals;
        this.enabledMaterials = enabledMaterials;
        this.checkEmpty = checkEmpty;
        this.changeWater = changeWater;
    }

    @Nullable
    private RedyeMaterial getEnabledMaterialByType(String itemType) {
        for (RedyeMaterial redyeMaterial : enabledMaterials) {
            if (itemType.endsWith(redyeMaterial.getColorMatName())) {
                return redyeMaterial;
            }
        }
        ConsoleMessage.debug(this.getClass(), plugin, "Could not find RedyeMaterial for " + itemType);
        return null;
    }

    @NotNull
    private Material getUndyeMaterial(RedyeMaterial redyeMaterial) {
        if (redyeMaterial.hasUndyeMatName()) {
            return Material.valueOf(redyeMaterial.getUndyeMatName());
        } else {
            return Material.valueOf("WHITE_" + redyeMaterial.getColorMatName());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onItemDrop(PlayerDropItemEvent e) {
        this.initiateTransformation(e.getItemDrop(), this.getEnabledMaterialByType(e.getItemDrop().getItemStack().getType().name()));
    }

    private void initiateTransformation(Item drop, RedyeMaterial redyeMaterial) {
        if (redyeMaterial == null) {
            ConsoleMessage.debug(CauldronItemDropListener.class, plugin, "Did not initiate transformation because of redyeMaterial == null.");
            return;
        }

        Material undyeMaterial = this.getUndyeMaterial(redyeMaterial);

        if (drop.getItemStack().getType().equals(undyeMaterial)) {
            ConsoleMessage.debug(CauldronItemDropListener.class, plugin, "Did not initiate transformation because of undyeMaterial == thrownMaterial.");
            return;
        }

        int maxStackSize = redyeMaterial.getInput();

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                ConsoleMessage.debug(CauldronItemDropListener.class, plugin, "Starting transformation...");
                Block cauldron = drop.getLocation().getBlock();
                if (cauldron.getBlockData().getMaterial().equals(internals.getWaterCauldron())) {
                    ConsoleMessage.debug(CauldronItemDropListener.class, plugin, "Item was thrown into a cauldron!");
                    Levelled cauldronData = (Levelled) cauldron.getBlockData();

                    int neededWater;
                    if (checkEmpty == 2)
                        neededWater = changeWater;
                    else
                        neededWater = checkEmpty;

                    ItemStack undyedItems = new ItemStack(drop.getItemStack());
                    undyedItems.setType(undyeMaterial);
                    undyedItems.setAmount(0);

                    int dyedAmount, water;
                    for (dyedAmount = drop.getItemStack().getAmount(), water = cauldronData.getLevel(); dyedAmount > 0 && neededWater <= Math.max(0, water); water = water - changeWater) {
                        int transformAmount = Math.min(dyedAmount, maxStackSize);
                        dyedAmount = dyedAmount - transformAmount;
                        undyedItems.setAmount(undyedItems.getAmount() + transformAmount);
                    }

                    if (water <= 0) {
                        internals.emptyCauldron(cauldron);
                    } else {
                        cauldronData.setLevel(water);
                        cauldron.setBlockData(cauldronData);
                    }

                    if (undyedItems.getAmount() > 0) {
                        ItemStack remainingDyedItems = new ItemStack(drop.getItemStack());
                        drop.setItemStack(undyedItems);
                        if (dyedAmount > 0) {
                            remainingDyedItems.setAmount(dyedAmount);
                            Objects.requireNonNull(drop.getLocation().getWorld()).dropItem(drop.getLocation(), remainingDyedItems);
                        }
                    }
                } else {
                    ConsoleMessage.debug(CauldronItemDropListener.class, plugin, "Item was NOT thrown into cauldron but " + drop.getLocation().getBlock().getBlockData().getMaterial().name());
                }
            }
        };
        task.runTaskLater(plugin, 20);
        cauldronDrops.put(drop, task);
    }

    private boolean cancelTransformation(Item item) {
        BukkitRunnable task = cauldronDrops.remove(item);
        if (task != null) {
            task.cancel();
            return true;
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDropMerge(ItemMergeEvent e) {
        boolean reschedule = this.cancelTransformation(e.getEntity());
        if (this.cancelTransformation(e.getTarget()) || reschedule) {
            this.initiateTransformation(e.getTarget(), this.getEnabledMaterialByType(e.getTarget().getItemStack().getType().name()));
            ConsoleMessage.debug(this.getClass(), plugin, "Transformation was rescheduled because of itemMerge.");
        }
    }
}
