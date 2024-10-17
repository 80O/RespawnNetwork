package com.respawnnetwork.respawnlib.gameapi;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a "container" for the game properties, kind of like the plugin.yml.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@Getter
public class GameDescriptor {
    /** The internal game id, used for database stuff */
    private final int id;

    /** The game name */
    private final String name;

    /** The authors / "inventors" of the game */
    private final List<String> authors;

    /** The developer(s) of the game */
    private final List<String> developers;


    /**
     * Creates a new game descriptor.
     *
     * @param config The config to read the values from
     */
    GameDescriptor(Logger logger, ConfigurationSection config) {
        if (config == null) {
            logger.warning("No description config found, please provide a valid \"game\" section in your game.yml!");

            id = 0;
            name = "<unknown>";
            authors = new LinkedList<>();
            developers = new LinkedList<>();

        } else {
            id = config.getInt("id", 0);
            name = config.getString("name", "<unknown>");
            authors = config.getStringList("authors");
            developers = config.getStringList("developers");
        }

        if (id <= 0) {
            logger.warning("Game ID is zero, please provide a valid \"game.id\" under the game config!");
        }
    }

}
