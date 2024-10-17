package me.tomshar.speedchallenge.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Tom on 12/03/14.
 */
public enum ItemUtils {
	TEAM_SELECTION		(Material.EMERALD, 1, 0, "§a§lTeam Selection", null),
	GAME_SETUP  		(Material.BOOK, 1, 0, "§4§lGame Setup", null),

	RED_TEAM		    (Material.WOOL, 1, 14, "§c§lJoin Red Team", null),
	BLUE_TEAM	        (Material.WOOL, 1, 11, "§9§lJoin Blue Team", null),
	YELLOW_TEAM		    (Material.WOOL, 1, 4, "§e§lJoin Yellow Team", null),
	GREEN_TEAM		    (Material.WOOL, 1, 5, "§2§lJoin Green Team", null),

	CHALLENGE_MODE      (Material.PAPER, 1, 0, "§lMode", null),
	CHALLENGE_VARIABLES (Material.PAPER, 1, 0, "§lVariables", null),
	CHALLENGE_SETUP     (Material.PAPER, 1, 0, "§lSetup", null);

	private final Material material;
	private final int amount;
	private final int data;
	private final String name;
	private List<String> lore;

	private final ItemStack product;

	private ItemUtils(Material material, int amount, int data, String name, List<String> lore) {
		this.material = material;
		this.amount = amount;
		this.data = data;
		this.name = name;
		this.lore = lore;

		this.product = makeProduct();
	}

	private ItemStack makeProduct() {
		ItemStack item = new ItemStack(material, amount);
		item.setDurability((short) data);

		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		item.setItemMeta(meta);

		return item;
	}

	public ItemStack getProduct() { return product; }

	public int getData() { return data; }

	public ItemUtils setLore(String... lore) {
		this.lore = Arrays.asList(lore);
		return this;
	}

}
