package net.respawn.goldrush.modules.shop.types;

import com.respawnnetwork.respawnlib.lang.Nameable;
import com.respawnnetwork.respawnlib.lang.ParseException;
import net.respawn.goldrush.modules.shop.ShopItem;
import org.bukkit.configuration.ConfigurationSection;

import java.util.logging.Logger;


public interface ShopType extends Nameable {

    ShopItem parseConfig(Logger logger, int price, ConfigurationSection itemConfig) throws ParseException;

}
