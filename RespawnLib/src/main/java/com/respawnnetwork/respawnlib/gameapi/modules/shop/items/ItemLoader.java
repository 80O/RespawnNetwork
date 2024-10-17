package com.respawnnetwork.respawnlib.gameapi.modules.shop.items;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopItem;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopLoader;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * The loader for shop items.
 * 
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class ItemLoader implements ShopLoader {

    @Override
    public ShopItem load(GameShop shop, ConfigurationSection section) {
        // Get information
        int price = section.getInt("price");

        if (price == 0) {
            shop.getModule().getLogger().warning("Item price cannot be zero! (section: " + section + ')');
            return null;
        }

        ItemStack item = Item.parseItem(shop.getModule().getLogger(), section.get("item"));

        // Create item
        return new PhysicalItem(shop, price, item);
    }

    @Override
    public String getDisplayName() {
        return "Shop Item";
    }

    @Override
    public String getName() {
        return "items";
    }

}
