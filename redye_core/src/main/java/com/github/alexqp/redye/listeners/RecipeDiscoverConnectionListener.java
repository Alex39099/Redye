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

package com.github.alexqp.redye.listeners;

import com.github.alexqp.commons.messages.ConsoleMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

public class RecipeDiscoverConnectionListener implements Listener {

    private final JavaPlugin plugin;
    private final HashSet<HashSet<NamespacedKey>> allKeys;

    private final boolean logout;

    public RecipeDiscoverConnectionListener(JavaPlugin plugin, HashSet<HashSet<NamespacedKey>> allKeys, boolean logout) {
        this.plugin = plugin;
        this.allKeys = allKeys;
        this.logout = logout;
    }

    @EventHandler
    private void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        for (HashSet<NamespacedKey> keys : allKeys) {
            if (keys.iterator().hasNext() && p.discoverRecipe(keys.iterator().next())) {
                p.discoverRecipes(keys);
            } else {
                ConsoleMessage.debug(this.getClass(), plugin, "LOGIN: skipped key bc first one was already discovered");
            }
        }
    }

    @EventHandler
    private void onPlayerLeave(PlayerQuitEvent e) {
        if (logout) {
            Player p = e.getPlayer();
            for (HashSet<NamespacedKey> keys : allKeys) {
                if (keys.iterator().hasNext() && p.undiscoverRecipe(keys.iterator().next())) {
                    p.undiscoverRecipes(keys);
                } else {
                    ConsoleMessage.debug(this.getClass(), plugin, "LOGOUT: skipped key bc first one was already discovered");
                }
            }
        }
    }
}
