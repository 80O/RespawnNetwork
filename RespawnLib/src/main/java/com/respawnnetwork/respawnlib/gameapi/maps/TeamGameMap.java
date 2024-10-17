package com.respawnnetwork.respawnlib.gameapi.maps;

import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a team based game map.
 *
 * @author spaceemotion
 * @since 1.0.1
 * @version 1.0
 */
public class TeamGameMap extends GameMap {

    /** The winning team */
    @Getter
    @Setter
    private Team winningTeam;


    /**
     * Creates a new team based game map.
     *
     * @param worldName The world name
     * @param config    The map configuration
     */
    public TeamGameMap(String worldName, ConfigurationSection config) {
        super(worldName, config);
    }

}
