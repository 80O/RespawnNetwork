package com.respawnnetwork.respawnlib.network.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * The listener for the inventory menu events.
 */
public class InventoryMenuEventListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMenuItemClicked(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory.getHolder() instanceof InventoryMenu) {
            event.setCancelled(true);

            InventoryMenu menu = (InventoryMenu) inventory.getHolder();

            if (event.getWhoClicked() instanceof Player) {
                Player player = (Player) event.getWhoClicked();
                int index = event.getRawSlot();

                if (index >= 0 && index < inventory.getSize()) {
                    menu.selectItem(player, index, event.isRightClick(), event.isShiftClick());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMenuClosed(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Inventory inventory = event.getInventory();

            if (inventory.getHolder() instanceof InventoryMenu) {
                InventoryMenu menu = (InventoryMenu) inventory.getHolder();

                InventoryMenuCloseBehaviour menuCloseBehaviour = menu.getMenuCloseBehaviour();

                if (menuCloseBehaviour != null) {
                    menuCloseBehaviour.onClose((Player) event.getPlayer());
                }
            }
        }
    }

}
