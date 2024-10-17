package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.armor;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectObjectProcedure;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Represents an upgrade for the full player armor.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class FullArmorUpgrade extends ArmorUpgrade {
    static final Collection<Integer> SLOTS = new ArrayList<>(4);
    static {
        SLOTS.add(0);
        SLOTS.add(1);
        SLOTS.add(2);
        SLOTS.add(3);
    }

    private final TMap<Enchantment, Integer> enchantments;


    public FullArmorUpgrade(ArmorUpgrade previous, ItemStack displayItem, int price) {
        super(previous, displayItem, price);

        this.enchantments = new THashMap<>();
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments() {
        return enchantments;
    }

    @Override
    public boolean canUpgrade(GamePlayer player) {
        return checkSlots(player.getPlayer(), SLOTS);
    }

    @Override
    public void upgrade(GamePlayer gamePlayer) {
        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        for (final ItemStack content : armorContents) {
            if (content == null) {
                continue;
            }

            // Apply enchantments
            enchantments.forEachEntry(new TObjectObjectProcedure<Enchantment, Integer>() {
                @Override
                public boolean execute(Enchantment enchantment, Integer level) {
                    if (!enchantment.canEnchantItem(content)) {
                        return true;
                    }

                    content.addEnchantment(enchantment, level);

                    return true;
                }
            });
        }

        inventory.setArmorContents(armorContents);
    }

}
