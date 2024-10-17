package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.armor;

import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades.Upgrade;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

/**
 * Represents an upgrade for player armor.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class ArmorUpgrade extends Upgrade<ArmorUpgrade> {

    protected ArmorUpgrade(ArmorUpgrade previous, ItemStack displayItem, int price) {
        super(previous, displayItem, price);
    }

    /**
     * Returns a map containing enchantments that are being added to the armor
     * on an upgrade.
     * <p />
     * If the upgrade consists of more than one armor piece, some enchantments
     * might got overwritten. This might return a non-modifiable map!
     *
     * @return The enchantments in a map
     */
    public abstract Map<Enchantment, Integer> getEnchantments();

    protected boolean checkSlots(Player player, Collection<Integer> slots) {
        if (player == null) {
            return false;
        }

        ItemStack[] armorContents = player.getInventory().getArmorContents();

        // We can upgrade if we have at least one armor piece on
        int count = 0;
        for(int slot : slots) {
            count += armorContents[slot] != null ? 1 : 0;
        }

        if (count == 0) {
            GameShop.MESSAGE.sendKey(player, GameShop.ERROR + "upgrade.noArmor");
            return false;
        }

        return true;
    }

}
