package com.respawnnetwork.respawnlib.gameapi.modules.gift;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents a gift.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
@Data
public class Gift {
    private final int frequency;
    private final List<ItemStack> items;
    private boolean immediately;
}
