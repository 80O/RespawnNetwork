package com.respawnnetwork.respawnlib.gameapi.modules.shop.items;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopItem;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a purchasable item for a game shop.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class PhysicalItem extends ShopItem {
    @Getter
    private final ItemStack displayItem;

    @Getter
    private final ItemStack itemStack;

    @Getter
    private final int price;

    private final String displayName;


    public PhysicalItem(GameShop shop, int price, ItemStack itemStack) {
        super(shop);

        this.itemStack = itemStack;
        this.price = price;

        if (itemStack == null) {
            this.displayItem = null;

        } else {
            this.displayItem = itemStack.clone();
            addPriceTag(displayItem, price);
        }

        this.displayName = Item.getHumanReadableName(itemStack);
    }

    @Override
    public boolean onBuyItem(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return false;
        }

        // Check if we have space first
        if (!Item.fitsInto(getItemStack(), player.getInventory())) {
            GameShop.MESSAGE.sendKey(player, GameShop.MSG_NOT_ENOUGH_SPACE);
            return false;
        }

        // Add item to inventory
        player.getInventory().addItem(getItemStack().clone());

        // Send success message
        GameShop.MESSAGE
                .provide("price", getPrice())
                .provide("currency", getShop().getCurrency().getDisplayName(getPrice()))
                .provide("count", getItemStack().getAmount())
                .provide("item", displayName)
                .sendKey(player, GameShop.MSG_SUCCESS);

        return true;
    }

    @Override
    public ItemStack getDisplayItem(GamePlayer player) {
        return getDisplayItem();
    }

    @Override
    public int getPrice(GamePlayer player) {
        return getPrice();
    }

}
