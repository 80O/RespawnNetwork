package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.atteo.evo.inflector.English;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an item in a shop.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class ShopItem {
    private final GameShop shop;
    private final String pluralString;


    protected ShopItem(GameShop shop) {
        this.shop = shop;

        this.pluralString = English.plural(shop.getCurrency().getDisplayName());
    }

    /**
     * Gets executed whenever a player buys this item.
     *
     * @param player The player that buys that item
     * @return True if we successfully bought the item, false if not
     */
    public abstract boolean onBuyItem(GamePlayer player);

    /**
     * Returns the item to display in the shop menu.
     *
     * @param player The player that opened the shop
     * @return The display item stack
     */
    public abstract ItemStack getDisplayItem(GamePlayer player);

    public abstract int getPrice(GamePlayer player);

    /**
     * Gets executed whenever the shop gets reset.
     * <p />
     * Implementations can add their own behavior.
     */
    public void reset() {
        // Nothing to do in the base class
    }

    public GameShop getShop() {
        return shop;
    }

    /**
     * Adds a "price tag" to the item.
     *
     * @param stack The item to modify
     */
    public void addPriceTag(ItemStack stack, int price) {
        if (stack == null) {
            return;
        }

        Item.addDescription(stack,
                ChatColor.GOLD + "Price: " + price + ' ' + getShop().getCurrency().getDisplayName(price)
        );
    }

}
