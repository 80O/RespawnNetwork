package com.respawnnetwork.respawnlib.network.menu;

import org.bukkit.entity.Player;

/**
 * Represents a menu close behaviour.
 * <p />
 * Defines what to do when a menu is closed by a player. Useful for menus which
 * MUST have an action before closing
 *
 * @author spaceemotion
 */
public interface InventoryMenuCloseBehaviour {

    /**
     * Called when a player closes a menu.
     *
     * @param player The player closing the menu
     */
    void onClose(Player player);

}
