package com.respawnnetwork.respawnlib.gameapi.modules.shop;

import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.lang.Nameable;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a loader for shop items, upgrades and more.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public interface ShopLoader extends Displayable, Nameable {

    /**
     * Loads a configuration section.
     *
     * @param shop The shop to load the stuff for
     * @param section The config section
     */
    ShopItem load(GameShop shop, ConfigurationSection section);

}
