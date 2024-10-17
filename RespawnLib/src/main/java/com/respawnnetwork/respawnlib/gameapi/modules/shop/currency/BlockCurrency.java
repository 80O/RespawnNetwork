package com.respawnnetwork.respawnlib.gameapi.modules.shop.currency;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.shop.GameShop;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a currency that takes blocks as "money".
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class BlockCurrency implements Currency {
    /** Gets the currency material. */
    @Getter
    private Material material;
    private String displayName;
    private String displayNamePlural;


    @Override
    public boolean matches(String name) {
        material = Material.matchMaterial(name);

        if (material != null) {
            displayName = Item.getHumanReadableName(material);
            displayNamePlural = Item.getPluralizedMaterialName(material, 1);

            return true;
        }

        return false;
    }

    @Override
    public boolean canBuy(GamePlayer gamePlayer, int amount) {
        Player player = gamePlayer.getPlayer();
        return player != null && material != null && player.getInventory().containsAtLeast(new ItemStack(material), amount);
    }

    @Override
    public boolean give(GamePlayer gamePlayer, int amount) {
        if (material == null) {
            return false;
        }

        Player player = gamePlayer.getPlayer();

        if (player == null) {
            return false;
        }

        Inventory inventory = player.getInventory();
        ItemStack item = new ItemStack(material, amount);

        if (Item.fitsInto(item, inventory)) {
            return inventory.addItem(item.clone()).isEmpty();

        } else {
            GameShop.MESSAGE.sendKey(player, GameShop.MSG_NOT_ENOUGH_SPACE);
            return false;
        }
    }

    @Override
    public boolean take(GamePlayer gamePlayer, int amount) {
        if (material == null) {
            return false;
        }

        Player player = gamePlayer.getPlayer();
        if (player == null) {
            return false;
        }

        // The normal remove method seems not to work quite right
        player.getInventory().removeItem(new ItemStack(material, amount));

        return true;
    }

    @Override
    public String getDisplayName() {
        return displayName == null ? "Item" : displayName;
    }

    @Override
    public String getDisplayName(int amount) {
        return amount == 1 ? getDisplayName() : displayNamePlural;
    }
}
