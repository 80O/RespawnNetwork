package net.respawn.goldrush.modules.shop.types;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.lang.ParseException;
import lombok.Getter;
import net.respawn.goldrush.modules.shop.ShopItem;
import net.respawn.goldrush.modules.shop.XPCurrency;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.logging.Logger;

/**
 * Represents an item type that gives normal items to players.
 */
public class ItemShopType implements ShopType {

    @Override
    public ShopItem parseConfig(Logger logger, int price, ConfigurationSection itemConfig) throws ParseException {
        ItemStack item = Item.parseItem(logger, itemConfig.get("item"));
        return item != null ? new PhysicalItem(price, item) : null;
    }

    @Override
    public String getName() {
        return "item";
    }


    private static class PhysicalItem extends ShopItem {
        @Getter
        private final ItemStack itemStack;
        private String displayName;


        private PhysicalItem(int price, ItemStack itemStack) {
            super(price);

            this.itemStack = itemStack;
            this.displayName = Item.getHumanReadableName(getItemStack());
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
            player.updateInventory();

            // Send success message
            GameShop.MESSAGE
                    .provide("price", getPrice())
                    .provide("currency", XPCurrency.INSTANCE.getDisplayName(getPrice()))
                    .provide("count", getItemStack().getAmount())
                    .provide("item", getDisplayName())
                    .sendKey(player, GameShop.MSG_SUCCESS);

            return true;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }


    }

}
