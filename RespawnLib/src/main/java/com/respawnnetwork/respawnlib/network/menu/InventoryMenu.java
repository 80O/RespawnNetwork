package com.respawnnetwork.respawnlib.network.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

/**
 * Allows custom minecraft inventories.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class InventoryMenu implements InventoryHolder {
    public final static int ROW_SIZE = 9;

    private InventoryMenuItem[] items;
    private Inventory inventory;
    private String title;
    private int size;
    private InventoryMenuCloseBehaviour closeBehaviour;


    public InventoryMenu(String title, int rows) {
        this.title = title;
        this.size = rows * ROW_SIZE;

        this.items = new InventoryMenuItem[size];
    }

    public void setMenuCloseBehaviour(InventoryMenuCloseBehaviour menuCloseBehaviour) {
        this.closeBehaviour = menuCloseBehaviour;

    }

    public InventoryMenuCloseBehaviour getMenuCloseBehaviour() {
        return closeBehaviour;
    }

	
	/* -------- Inventory related functions -------- */

    public Inventory getInventory() {
        if (inventory == null) {
            inventory = Bukkit.createInventory(this, size, title);
        }

        return inventory;
    }

    public boolean addItem(InventoryMenuItem item, int x, int y) {
        return addItem(item, y * ROW_SIZE + x);
    }

    public boolean addItem(InventoryMenuItem item, int index) {
        ItemStack slot = getInventory().getItem(index);

        if (slot != null && slot.getType() != Material.AIR) {
            return false;
        }

        items[index] = item;
        item.setMenu(this);
        item.setIndex(index);

        getInventory().setItem(index, item.getItemStack());

        return true;
    }

    public void removeItem(int index) {
        items[index] = null;
        getInventory().setItem(index, null);
    }

    void setItem(InventoryMenuItem item, int index) {
        items[index] = item;
        getInventory().setItem(index, item.getItemStack());
    }

    void selectItem(Player player, int index) {
        selectItem(player, index, false, false);
    }

    void selectItem(Player player, int index, boolean right, boolean shift) {
        InventoryMenuItem item = items[index];

        if (item != null) {
            item.onClick(player, right, shift);
        }
    }

    public void openMenu(Player player) {
        if (getInventory().getViewers().contains(player)) {
            throw new IllegalArgumentException(player.getName() + " is already viewing " + getInventory().getTitle());
        }

        player.openInventory(getInventory());
    }

    public void updateMenu() {
        for (HumanEntity entity : getInventory().getViewers()) {
            ((Player) entity).updateInventory();
        }
    }

    public void closeMenu() {
        for (HumanEntity viewer : new LinkedList<>(getInventory().getViewers())) {
            closeMenu(viewer);
        }
    }

    public void closeMenu(HumanEntity viewer) {
        if (!getInventory().getViewers().contains(viewer)) {
            return;
        }

        InventoryCloseEvent event = new InventoryCloseEvent(viewer.getOpenInventory());
        Bukkit.getPluginManager().callEvent(event);
        viewer.closeInventory();
    }

    public static int calcSize(double slots) {
        return (int) Math.ceil((slots/* + 1*/) / (double) ROW_SIZE);
        // return (int) Math.ceil((1 + slots) / ROW_SIZE) * ROW_SIZE;
    }
	
	/* -------- General override functions -------- */

    @Override
    public String toString() {
        return "InventoryMenu{title=" + title + "; size=" + size + "}";
    }

}
