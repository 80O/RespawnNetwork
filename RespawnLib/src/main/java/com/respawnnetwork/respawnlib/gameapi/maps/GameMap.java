package com.respawnnetwork.respawnlib.gameapi.maps;

import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.lang.Nameable;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents a map in a game.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class GameMap implements Displayable, Nameable {
    /** The default maps configuration file name, including the file suffix */
    public static final String CONFIG_FILE_NAME = "maps.yml";

    /** The key for the section holding the default map configuration */
    public static final String KEY_DEFAULT_CONFIG = "defaults";

    /** The ticks for time day */
    public static final int TIME_DAY_TICKS = 0;

    /** The ticks for time night */
    public static final int TIME_NIGHT_TICKS = 12500;

    private static final String KEY_CREATORS = "creators";
    private static final String KEY_DEFAULT_TIME = "defaultTime";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_GAME_CONFIG = "config";
    private static final String KEY_GAMERULES = "gameRules";
    private static final String KEY_MAP_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SPAWN_LOCATION = "spawnLocation";

    /** The name of the world this map plays in */
    private final String worldName;

    /** The display name for the map, if not set this will hold the default world name */
    private String displayName;

    /** The creator(s) of the map */
    @Getter
    private List<String> creators;

    /** The configuration for this map */
    @Getter
    private final ConfigurationSection config;

    /** The configuration to use when creating a new game with this map */
    @Getter
    private final ConfigurationSection gameConfig;

    /** A map containing all game rules */
    private Map<String, String> gameRules;

    /** The bukkit world */
    private World world;

    /** The map id, used for database stuff */
    @Getter
    private int id;

    /** The map's spawn location */
    private Location spawnLocation;

    /** The world's difficulty */
    @Getter
    private Difficulty difficulty;

    /** The default time to set this world to */
    private long defaultTime;


    /**
     * Creates a new game map instance.
     *
     * @param worldName The name of the map's world folder
     * @param config The map configuration
     */
    public GameMap(String worldName, ConfigurationSection config) {
        this.worldName = worldName;
        this.config = config;

        this.gameRules = new THashMap<>();

        // Build game config
        if (config.isConfigurationSection(KEY_GAME_CONFIG)) {
            this.gameConfig = config.getConfigurationSection(KEY_GAME_CONFIG);

        } else {
            this.gameConfig = config.createSection(KEY_GAME_CONFIG);
        }
    }

    @Override
    public String getName() {
        return worldName;
    }

    @Override
    public String getDisplayName() {
        return displayName != null ? displayName : getName();
    }

    /**
     * Gets the world of the map. Returns null if the map hasn't been loaded yet.
     *
     * @return The bukkit world
     */
    @Nullable
    public World getWorld() {
        return world;
    }

    /**
     * Returns the spawn location for this map.
     * If the spawn location hasn't been set, this will return the default world spawn.
     *
     * @return The map spawn location
     */
    @Nullable
    public Location getSpawnLocation() {
        if (world != null && spawnLocation == null) {
            return world.getSpawnLocation();
        }

        return spawnLocation;
    }

    /**
     * Indicates whether or not this map is loaded.
     *
     * @return True if the map got loaded, false if not
     */
    public boolean isLoaded() {
        return world != null;
    }

    /**
     * Loads the map using the given map loader.
     * <p />
     * This will not do anything if the map has been loaded already.
     * On a successful load, this will return true, if something couldn't load properly this
     * will return false.
     *
     * @param mapLoader The map loader to use
     */
    public boolean load(MapLoader mapLoader) {
        // Don't do stuff when we're already loaded
        if (world != null) {
            return true;
        }

        world = mapLoader.loadMap(this);

        // Don't continue if we didn't get a map
        if (world == null) {
            return false;
        }

        world.setDifficulty(difficulty);
        world.setTime(defaultTime);

        // Set gamerules
        for (Map.Entry<String, String> gameRule : gameRules.entrySet()) {
            mapLoader.getLogger().info("Setting gamerule " + gameRule.getKey() + " to " + gameRule.getValue());
            world.setGameRuleValue(gameRule.getKey(), gameRule.getValue());
        }

        // Set spawn location
        spawnLocation.setWorld(world);

        // Phew, we're done!
        return true;
    }

    /**
     * Loads the required data from the map config and returns true if the map could
     * successfully be loaded.
     *
     * @param logger The logger to use for error messages
     * @return True on a successful load, false if errors occurred
     */
    public boolean loadConfig(Logger logger) {
        boolean success = true;

        // Get map ID and warn on wrong map ID
        this.id = config.getInt(KEY_MAP_ID, 0);
        if (getId() <= 0) {
            logger.warning("Map ID is zero, please provide a valid \"map.id\" under maps.yml!");
        }

        // Get Display name, no big deal if this is null (see above)
        this.displayName = config.getString(KEY_NAME);

        // Get list of map creators
        this.creators = config.getStringList(KEY_CREATORS);

        // Load spawn location
        if (config.isConfigurationSection(KEY_SPAWN_LOCATION)) {
            this.spawnLocation = new com.respawnnetwork.respawnlib.bukkit.Location(
                    config.getConfigurationSection(KEY_SPAWN_LOCATION).getValues(false)
            );

        } else {
            logger.severe("No spawn location set, won't be able to use map");
            success = false;
        }

        // Load map difficulty (vanilla difficulty)
        String difficultyName = config.getString(KEY_DIFFICULTY, "Peaceful").toUpperCase();
        this.difficulty = Difficulty.valueOf(difficultyName);

        if (difficulty == null) {
            logger.info("Invalid difficulty set, will use peaceful: " + difficultyName);
        }

        // Load default world time, defaults to "day"
        this.defaultTime = config.getLong(KEY_DEFAULT_TIME, TIME_DAY_TICKS);

        // Load game rules
        ConfigurationSection gameruleSection = config.getConfigurationSection(KEY_GAMERULES);
        if (gameruleSection != null) {
            for (String rule : gameruleSection.getKeys(false)) {
                gameRules.put(rule, gameruleSection.getString(rule));
            }
        }

        return success;
    }

}
