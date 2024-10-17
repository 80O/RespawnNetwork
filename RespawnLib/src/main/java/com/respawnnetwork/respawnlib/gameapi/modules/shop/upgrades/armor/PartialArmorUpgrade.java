package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.armor;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import gnu.trove.map.hash.THashMap;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;
import java.util.Set;

/**
 * Represents an upgrade for parts of the player armor.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class PartialArmorUpgrade extends ArmorUpgrade {
    private final Map<Integer, Map<Enchantment, Integer>> enchantments;


    public PartialArmorUpgrade(ArmorUpgrade previous, ItemStack displayItem, int price) {
        super(previous, displayItem, price);

        this.enchantments = new THashMap<>();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        Map<Enchantment, Integer> enchants = new THashMap<>();

        for (Map<Enchantment, Integer> map : enchantments.values()) {
            enchants.putAll(map);
        }

        return enchants;
    }

    @Override
    public boolean canUpgrade(GamePlayer player) {
        return checkSlots(player.getPlayer(), enchantments.keySet());
    }

    @Override
    public void upgrade(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        for (Map.Entry<Integer, Map<Enchantment, Integer>> entry : enchantments.entrySet()) {
            ItemStack itemStack = armorContents[entry.getKey()];

            if (itemStack == null) {
                continue;
            }

            itemStack.addEnchantments(entry.getValue());
        }

        inventory.setArmorContents(armorContents);
    }

    public Set<Integer> getAffectedSlots() {
        return enchantments.keySet();
    }

    public void setEnchantments(int slot, Map<Enchantment, Integer> enchantments) {
        this.enchantments.put(slot, enchantments);
    }

}
