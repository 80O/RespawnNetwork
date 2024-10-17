package com.respawnnetwork.respawnlib.gameapi.modules.shop.upgrades;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;

/**
 * Represents an upgrade used by an item upgrade.
 * <p />
 * This holds the original item stack, the display item and the display string
 * for messages.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class Upgrade<U> {
    @Getter
    private final U previous;

    @Getter
    private final ItemStack displayItem;

    @Getter
    private final String displayString;

    @Getter
    private final int price;


    protected Upgrade(U previous, ItemStack displayItem, int price) {
        this.previous = previous;
        this.displayItem = displayItem;
        this.price = price;

        if (displayItem != null) {
            this.displayString = Item.getPluralizedMaterialName(displayItem.getType(), displayItem.getAmount());

        } else {
            // We sadly have to do something like this :(
            this.displayString = "<Unknown Item>";
        }
    }

    public abstract boolean canUpgrade(GamePlayer player);

    public abstract void upgrade(GamePlayer player);

}
