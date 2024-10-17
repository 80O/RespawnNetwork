package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.item;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.Upgrade;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an upgrade for items.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class ItemUpgrade extends Upgrade<ItemUpgrade> {
    @Getter
    private final ItemStack itemStack;


    public ItemUpgrade(ItemUpgrade previous, ItemStack itemStack, ItemStack displayItem, int price) {
        super(previous, displayItem, price);

        this.itemStack = itemStack;
    }

    @Override
    public boolean canUpgrade(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return false;
        }

        if (!Item.fitsInto(getItemStack(), player.getInventory())) {
            GameShop.MESSAGE.sendKey(player, GameShop.MSG_NOT_ENOUGH_SPACE);
            return false;
        }

        return true;
    }

    @Override
    public void upgrade(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return;
        }

        Inventory inventory = player.getInventory();

        // Okay we for sure have the space, so first remove the previous item
        if (getPrevious() != null) {
            for (int i = inventory.getContents().length - 1; i >= 0; i--) {
                ItemStack stack = inventory.getItem(i);

                // Replace old "similar" item with new one
                if (isSimilar(getPrevious().getItemStack(), stack)) {
                    inventory.setItem(i, getItemStack());

                    // Return after one upgrade, so we don't upgrade all similar items
                    return;
                }
            }

        } else {
            // ... or just add the new one
            inventory.addItem(getItemStack().clone());
        }
    }

    private boolean isSimilar(ItemStack first, ItemStack second) {
        return !(first == null || second == null) &&
                (first.getType().equals(second.getType())) &&
                (first.hasItemMeta() == second.hasItemMeta()) &&
                (!first.hasItemMeta() || Bukkit.getItemFactory().equals(first.getItemMeta(), second.getItemMeta()));
    }

}
