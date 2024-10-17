package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades;

import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.lang.Nameable;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

/**
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public interface UpgradeTypeLoader<U extends Upgrade, T extends UpgradeableShopItem<U>> extends Nameable {

    @Nullable
    T createShopItem(GameShop shop, ConfigurationSection config);

    U loadUpgrade(T shopItem, U previous, int price, ConfigurationSection config);

}
