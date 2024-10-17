package com.respawnnetwork.respawnlib.network.menu;

import com.respawnnetwork.respawnlib.bukkit.Item;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item in an inventory menu.
 * <p />
 * An item in a custom menu. Displays like an ItemStack in an inventory, and
 * activates the onClick method when it's getting selected. It defines what
 * to do when a "button" in a menu is being clicked by a player. Useful for
 * menus which MUST have an action when clicking a button.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public abstract class InventoryMenuItem {
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private InventoryMenu menu;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int index;

    @Getter
    private ItemStack itemStack;


    public InventoryMenuItem(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public InventoryMenuItem(String text) {
        this(text, Material.PAPER);
    }

    public InventoryMenuItem(String text, Material icon) {
        this(text, icon, 1);
    }

    public InventoryMenuItem(String text, Material icon, int amount) {
        itemStack = Item.getFor(icon, text);
        itemStack.setAmount(amount);
    }

    /**
     * Called when a player clicks on a button in a menu
     *
     * @param player The player clicking the button
     * @param isRightClick True if the right mouse button has been pressed
     * @param isShiftClick True if the shift key has been pressed
     */
    protected abstract void onClick(Player player, boolean isRightClick, boolean isShiftClick);

//    /**
//     * Attempts to move the item to a different index.
//     *
//     * @param i The new index
//     * @return True if successful, false if not
//     */
//    public boolean setIndex(int i) {
//        if (getMenu() == null) {
//            return false;
//        }
//
//        Inventory inventory = getMenu().getInventory();
//
//        // Check inventory size
//        if (i > (inventory.getSize() - 1)) {
//            return false;
//        }
//
//        // We can't really move a different item
//        if (inventory.getItem(i) != null) {
//            return false;
//        }
//
//        getMenu().removeItem(getIndex());
//        getMenu().setItem(this, i);
//
//        return true;
//    }

    public int getAmount() {
        return itemStack.getAmount();
    }

    public Material getIcon() {
        return itemStack.getType();
    }

    public void setIcon(Material material) {
        itemStack.setType(material);
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;

        if (getMenu() != null) {
            getMenu().getInventory().setItem(getIndex(), getItemStack());
        }
    }

}
