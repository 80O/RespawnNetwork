package me.tomshar.speedchallenge.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Tom on 12/03/14.
 */

@SuppressWarnings("deprecation")
public class ItemName {

	public static String convert(ItemStack item) {

		if(item.getType() == Material.WOOL) {
			return DyeColor.getByWoolData((byte) item.getDurability()).name() + " WOOL";
		}

		// Nothing to convert so just return normal name.
		return item.getType().name();
	}

}
