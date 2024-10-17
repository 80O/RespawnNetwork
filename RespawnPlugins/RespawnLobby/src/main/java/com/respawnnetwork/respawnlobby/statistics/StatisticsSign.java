package com.respawnnetwork.respawnlobby.statistics;

import lombok.Data;
import org.bukkit.block.Sign;

import java.util.List;

/**
 * A sign for custom server statistics
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
@Data
public class StatisticsSign {
    /** The sign block data */
    private final Sign sign;

    /** */
    private final List<String> lines;

}
