package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopItem;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents an upgradeable shop item.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class UpgradeableShopItem<U extends Upgrade> extends ShopItem {
    public static final ItemStack EMPTY_SLOT = Item.getFor(
            Material.THIN_GLASS,
            ChatColor.GRAY + "No more upgrades available"
    );

    /** Returns the upgrade type for this shop item. */
    @Getter
    private final UpgradeTypeLoader type;

    private final Map<GamePlayer, Integer> customerLevels;

    /** Returns a list of all possible upgrades. */
    @Getter
    private final List<U> upgrades;


    public UpgradeableShopItem(GameShop shop, UpgradeTypeLoader type) {
        super(shop);

        this.type = type;
        this.customerLevels = new THashMap<>();
        this.upgrades = new LinkedList<>();
    }

    @Override
    public boolean onBuyItem(GamePlayer player) {
        int level = getUpgradeLevel(player);

        if (level < 0) {
            return false;
        }

        // Get upgrade item
        Upgrade upgrade = getUpgrades().get(level);

        // Check and apply upgrade
        if (!upgrade.canUpgrade(player)) {
            return false;
        }

        upgrade.upgrade(player);

        // Increase (and set level to -1 when we hit the last upgrade) and save the level
        level++;

        if (level >= getUpgrades().size()) {
            level = -1;
        }

        customerLevels.put(player, level);

        // Send success message
        GameShop.MESSAGE
                .provide("price", upgrade.getPrice())
                .provide("currency", getShop().getCurrency().getDisplayName(upgrade.getPrice()))
                .provide("item", upgrade.getDisplayString())
                .provide("type", getType().getName())
                .sendKey(player.getPlayer(), GameShop.MSG_SUCCESS_UPGRADE);

        return true;
    }

    @Override
    public void reset() {
        // Clear all levels
        customerLevels.clear();
    }

    public void reset(GamePlayer player) {
        customerLevels.remove(player);
    }

    @Override
    public ItemStack getDisplayItem(GamePlayer player) {
        int level = getUpgradeLevel(player);

        if (level < 0) {
            // He bought all the upgrades, show the "empty" item
            return EMPTY_SLOT;
        }

        return getUpgrades().get(level).getDisplayItem();
    }

    @Override
    public int getPrice(GamePlayer player) {
        int level = getUpgradeLevel(player);

        // Return zero for non-existent upgrades
        if (level < 0) {
            return 0;
        }

        // Return the upgrade price
        return getUpgrades().get(level).getPrice();
    }

    /**
     * Gets the upgrade level by the given player.
     * <p />
     * This returns zero if the player never bought an upgrade,
     * or -1 if he bought all possible upgrades.
     *
     * @param player The player
     * @return The upgrade level, or -1
     */
    public int getUpgradeLevel(GamePlayer player) {
        Integer level = customerLevels.get(player);

        if (level == null) {
            // The player never bought an upgrade, so set that to zero
            level = 0;
        }

        // Check for out of bounds
        if (level >= getUpgrades().size()) {
            return -1;
        }

        return level;
    }

}
