package net.respawn.slicegames;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.maps.GameMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a slice games map.
 * Contains chests and their loot as well as the winner and the start time.
 *
 * @author spaceemotion
 * @version 1.0
 */
@Getter
@Setter
public class SGMap extends GameMap {
    /** The winner of this map */
    private SGPlayer winner;

    /** The time we started this map */
    private Timestamp startTime;


    /**
     * Creates a new slice games map.
     *
     * @param worldName    The world name
     * @param config       The map configuration
     */
    public SGMap(String worldName, ConfigurationSection config) {
        super(worldName, config);
    }

    /**
     * Gets the time difference between the start of the map and now.
     * If the map hasn't been started yet (start time is null), this will return 0;
     *
     * @return The elapsed time
     */
    public Date getElapsedTime() {
        return new Date(startTime != null ? System.currentTimeMillis() - startTime.getTime() : 0L);
    }

}
