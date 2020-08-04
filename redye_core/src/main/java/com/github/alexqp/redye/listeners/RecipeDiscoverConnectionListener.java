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

    private JavaPlugin plugin;
    private HashSet<HashSet<NamespacedKey>> allKeys;

    private boolean logout;

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
