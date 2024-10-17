package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.item;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.UpgradeTypeLoader;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.UpgradeableShopItem;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a purchasable upgrade.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class ShopItemUpgrade extends UpgradeableShopItem<ItemUpgrade> implements Listener {
    /**
     * Returns the start item every player has in the inventory at
     * the beginning of the game.
     */
    @Getter
    private final ItemStack startItem;


    public ShopItemUpgrade(GameShop shop, UpgradeTypeLoader type, ItemStack startItem) {
        super(shop, type);

        this.startItem = startItem;

        // Register events
        shop.getModule().registerListener(this);
    }

    @Override
    public void reset(GamePlayer player) {
        super.reset(player);

        Player bukkitPlayer = player.getPlayer();
        if (bukkitPlayer != null) {
            bukkitPlayer.getInventory().addItem(getStartItem().clone());
        }
    }


    public static class Loader implements UpgradeTypeLoader<ItemUpgrade, ShopItemUpgrade> {

        @Override
        public ShopItemUpgrade createShopItem(GameShop shop, ConfigurationSection config) {
            // Get start item
            ItemStack startItem = Item.parseItem(shop.getModule().getLogger(), config.get("startItem"));

            if (startItem == null) {
                // No need to log something here, this has already been covered by the item parsing
                return null;
            }

            // Create new shop item
            ShopItemUpgrade item = new ShopItemUpgrade(shop, this, startItem);

            // Also make the start item an "upgrade"
            item.getUpgrades().add(new ItemUpgrade(null, item.getStartItem(), null, 0));

            return item;
        }

        @Override
        public ItemUpgrade loadUpgrade(ShopItemUpgrade shopItem, ItemUpgrade previous, int price, ConfigurationSection config) {
            ItemStack item = Item.parseItem(
                    shopItem.getShop().getModule().getLogger(),
                    config.get("item")
            );

            if (item == null) {
                return null;
            }

            if (previous == null) {
                // We know we added this beforehand
                previous = shopItem.getUpgrades().get(0);

                // Add remove it again, "it served its purpose, mwahahahaha"
                shopItem.getUpgrades().remove(0);
            }

            // Create display item
            ItemStack displayItem = item.clone();

            if (previous != null) {
                Item.addDescription(displayItem,
                        ChatColor.RESET + "Upgrade from:",
                        ChatColor.RESET + Item.getHumanReadableName(previous.getItemStack())
                );
            }

            // Create and add upgrade
            return new ItemUpgrade(previous, item, displayItem, price);
        }

        @Override
        public String getName() {
            return "item";
        }

    }

}
