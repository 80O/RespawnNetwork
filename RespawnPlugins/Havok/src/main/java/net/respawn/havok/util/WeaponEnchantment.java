package net.respawn.havok.util;

import org.bukkit.enchantments.Enchantment;

/**
 * Created by Tom on 19/03/14.
 */
public class WeaponEnchantment {

	private final Enchantment enchantment;
	private final int level;

	public WeaponEnchantment(Enchantment enchantment, int level) {
		this.enchantment = enchantment;
		this.level = level;
	}

	public Enchantment getEnchantment() {
		return enchantment;
	}

	public int getLevel() {
		return level;
	}
}
