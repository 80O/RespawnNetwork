package net.respawn.havok;

import net.respawn.havok.util.WeaponEnchantment;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * Created by Tom on 19/03/14.
 */
public class Weapon {

	private final ItemStack item;
	private final Material material;
	private final int amount;
	private final String name;
	private final String[] lore;
	private final WeaponEnchantment[] enchantments;
	private final double cooldown;
	private final double durability;
	private boolean isOnCooldown;

	public Weapon(Material material, int amount, String name, String[] lore, double cooldown, double durability, WeaponEnchantment... enchantments) {
		this.material = material;
		this.amount = amount;
		this.name = name;
		this.lore = lore;
		this.enchantments = enchantments;
		this.cooldown = cooldown;
		this.durability = durability;

		item = new ItemStack(material, amount);
		item.setDurability((short) durability);

		if(enchantments != null)
			for(WeaponEnchantment ench : enchantments)
				item.addUnsafeEnchantment(ench.getEnchantment(), ench.getLevel());

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(Arrays.asList(lore));

		item.setItemMeta(meta);

	}

	public ItemStack getItem() {
		return item;
	}

	public Material getMaterial() {
		return material;
	}

	public int getAmount() {
		return amount;
	}

	public String getName() {
		return name;
	}

	public String[] getLore() {
		return lore;
	}

	public WeaponEnchantment[] getEnchantments() {
		return enchantments;
	}

	public double getCooldown() {
		return cooldown;
	}

	public boolean isOnCooldown() {
		return isOnCooldown;
	}

	public void setOnCooldown(boolean isOnCooldown) {
		this.isOnCooldown = isOnCooldown;
	}

	public double getDurability() {
		return durability;
	}
}
