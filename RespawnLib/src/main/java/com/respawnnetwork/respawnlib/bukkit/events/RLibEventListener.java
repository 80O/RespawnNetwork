package com.respawnnetwork.respawnlib.bukkit.events;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * The default event listener for the library.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class RLibEventListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Bukkit.getPluginManager().callEvent(new EntityKillsEntityEvent(false, event));
    }

}
