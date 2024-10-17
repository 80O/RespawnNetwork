package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades;

import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopItem;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.ShopLoader;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.armor.UpgradeableArmor;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.item.ShopItemUpgrade;
import gnu.trove.map.hash.THashMap;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The loader for shop upgrades.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class UpgradeLoader implements ShopLoader {
    private final Map<String, UpgradeTypeLoader> types;


    public UpgradeLoader() {
        this.types = new THashMap<>();

        addSupport(new ShopItemUpgrade.Loader());
        addSupport(new UpgradeableArmor.Loader());
    }

    /**
     * Adds support for an upgradable shop item.
     *
     * @param loader The upgrade type loader
     */
    public void addSupport(UpgradeTypeLoader loader) {
        types.put(loader.getName(), loader);
    }

    @Override
    @SuppressWarnings("unchecked")
    public ShopItem load(GameShop shop, ConfigurationSection section) {
        Logger logger = shop.getModule().getLogger();

        String typeString = section.getString("type");
        if (typeString == null) {
            logger.warning("No upgrade type set, skipping item");
            return null;
        }

        UpgradeTypeLoader type = types.get(typeString);
        if (type == null) {
            logger.warning("Unknown upgrade type '" + typeString + "', skipping item");
            return null;
        }

        // Create shop item
        UpgradeableShopItem shopItem = type.createShopItem(shop, section);
        if (shopItem == null) {
            logger.warning("Errors loading upgrade shop item, skipping");
            return null;
        }

        // Load levels
        List<Map<?, ?>> list = section.getMapList("levels");

        if (list == null) {
            logger.warning("No levels set, skipping");
            return null;
        }

        Upgrade previous = null;

        for (Map<?, ?> obj : list) {
            // Create temp. section
            ConfigurationSection config = createSection(obj);

            // Get price
            int price = config.getInt("price");

            if (price == 0) {
                logger.warning("Price cannot be zero, skipping level entry!");
                continue;
            }

            // Load level config in upgrade itself
            Upgrade upgrade = type.loadUpgrade(shopItem, previous, price, config);

            if (upgrade == null) {
                // Errors occurred, skip this then
                continue;
            }

            // Add upgrade
            shopItem.addPriceTag(upgrade.getDisplayItem(), upgrade.getPrice());
            shopItem.getUpgrades().add(upgrade);

            // Set the previous upgrade to the new one
            previous = upgrade;
        }

        return shopItem;
    }

    @Override
    public String getDisplayName() {
        return "Upgrade";
    }

    @Override
    public String getName() {
        return "upgrades";
    }

    private ConfigurationSection createSection(Map<?, ?> contents) {
        MemoryConfiguration memory = new MemoryConfiguration();

        return memory.createSection("tmp", contents);
    }

}
