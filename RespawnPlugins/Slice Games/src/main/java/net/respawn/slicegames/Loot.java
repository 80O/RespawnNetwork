package net.respawn.slicegames;

import lombok.Data;
import org.bukkit.Material;

/**
 * Represents an item found in chests.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
@Data
public class Loot {
    private final Material item;
    private final int min;
    private final int max;
    private final double weight;
}
