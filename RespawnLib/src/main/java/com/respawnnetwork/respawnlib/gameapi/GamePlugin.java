package com.respawnnetwork.respawnlib.gameapi;

import com.respawnnetwork.respawnlib.gameapi.maps.GameMap;
import com.respawnnetwork.respawnlib.gameapi.maps.MapCycle;
import com.respawnnetwork.respawnlib.gameapi.maps.MapCycleConstructor;
import com.respawnnetwork.respawnlib.gameapi.maps.MapLoader;
import com.respawnnetwork.respawnlib.plugin.Plugin;
import com.respawnnetwork.respawnlib.plugin.PluginConfig;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents a plugin that uses the game API.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
@Getter
public abstract class GamePlugin<G extends Game, M extends GameMap> extends Plugin {
    private final Map<String, MapCycleConstructor<M>> cycleConstructors;

    /** The map configuration file */
    private PluginConfig mapsConfig;

    /** The map loader we use to copy maps from the world template directory */
    private MapLoader mapLoader;

    /** The list of maps this plugin holds */
    private List<M> maps;

    /** The map cycle we're using */
    @Setter
    @Nullable
    private MapCycle<M> mapCycle;

    /** The current game instance */
    private G game;

    /** The game config file */
    private PluginConfig gameConfig;


    /**
     * Creates a new (abstract) game plugin instance.
     * <p />
     * This automatically registers all default map cycles.
     */
    protected GamePlugin() {
        super();

        this.cycleConstructors = new THashMap<>();

        // Register default map cycles
        registerMapCycle("once", new MapCycleConstructor<M>() {
            @Override
            public MapCycle<M> construct(List<M> maps) {
                return new MapCycle.Once<>(maps);
            }
        });

        registerMapCycle("linear", new MapCycleConstructor<M>() {
            @Override
            public MapCycle<M> construct(List<M> maps) {
                return new MapCycle.Linear<>(maps);
            }
        });

        registerMapCycle("random", new MapCycleConstructor<M>() {
            @Override
            public MapCycle<M> construct(List<M> maps) {
                return new MapCycle.Randomized<>(maps);
            }
        });
    }

    /**
     * Creates a new game instance
     *
     * @param map The map this game is played in
     * @param gameConfig The configuration used for the game
     * @return The newly created game instance
     */
    protected abstract G createGame(M map, ConfigurationSection gameConfig);

    /**
     * Creates a new map instance
     *
     * @param name The name of the world folder
     * @param mapConfig The map configuration
     * @return The newly created map instance
     */
    protected abstract M createMap(String name, ConfigurationSection mapConfig);

    /**
     * Gets executed whenever a game has finished.
     *
     * @param game The game that just finished
     */
    protected void onGameEnd(G game) {
        if (!isEnabled()) {
            return;
        }

        getPluginLog().info("Finished game with map '" + game.getMap().getDisplayName() + "', getting next map using the map cycle...");
        runNextGame();
    }

    /**
     * Gets the next map from the map cycle.
     *
     * @return The next map
     * @see #getMaps()
     * @see #getMapCycle()
     */
    public M getNextMap() {
        if (mapCycle == null) {
            return null;
        }

        return mapCycle.getNext();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        // Load messages
        loadMessages(Game.CONFIG_FILE_NAME);

        // Load global game config
        this.gameConfig = getConfig(Game.CONFIG_FILE_NAME);

        // Get map config stuff
        this.maps = new LinkedList<>();
        this.mapsConfig = getConfig(GameMap.CONFIG_FILE_NAME);
        String templateFolder = getMapsConfig().getString("templateFolder");
        this.mapLoader = new MapLoader(getPluginLog(), templateFolder != null ? new File(templateFolder) : null);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        // Load maps
        ConfigurationSection mapSection = mapsConfig != null ? mapsConfig.getConfigurationSection("maps") : null;
        if (mapSection != null) {
            for (String key : mapSection.getKeys(false)) {
                ConfigurationSection mapCfg = buildMapConfiguration(mapSection.getConfigurationSection(key));

                if (mapCfg == null) {
                    continue;
                }

                // Create game map
                M map = createMap(key, mapCfg);

                // Load map configuration
                if (map.loadConfig(getPluginLog())) {
                    getPluginLog().info("Successfully added map '" + map.getDisplayName() + "' to map cycle!");
                    maps.add(map);

                } else {
                    getPluginLog().warning("Error loading map '" + map.getName() + "' will not add to map cycle!");
                }
            }
        }

        // Add default server map if empty
        // SpaceEmotion 02.05.2014: Removed that for now
        /*if (getMaps().isEmpty()) {
            ConfigurationSection section = buildMapConfiguration(new MemoryConfiguration());

            // Just use the default world
            String name = getServer().getWorlds().get(0).getName();
            section.set(name + ".id", 0);

            M map = createMap(name, section);
            getPluginLog().info("Added default server map to map cycle!");

            maps.add(map);
        }*/

        // Log number of loaded maps
        getPluginLog().info("Loaded " + getMaps().size() + " map(s) successfully");

        // Load map cycle
        String cycleName = mapsConfig == null ? "linear" : mapsConfig.getString("cycle", "linear");
        MapCycleConstructor<M> mapCycleConstructor = getMapCycle(cycleName);

        if (mapCycleConstructor == null) {
            this.mapCycle = new MapCycle.Linear<>(getMaps());
            cycleName = MapCycle.DEFAULT_NAME;

            getPluginLog().warning("Unknown map cycle type '" + cycleName + "', falling back to '" + cycleName + "'");

        } else {
            this.mapCycle = mapCycleConstructor.construct(getMaps());
        }

        getPluginLog().info("Using '" + cycleName + "' map cycling");

        if (getMapCycle() == null) {
            shutdown("Could not create map cycle from constructor");
            return;
        }

        // Check needed amount of maps
        int neededMaps = getMapCycle().getMinimumNeededMaps();
        if (getMaps().size() < neededMaps) {
            shutdown("Map cycle needs at least " + neededMaps + " map(s)");
            return;
        }

        // Automatically run the first (next) game
        runNextGame();
    }

    @Override
    public void onReload() {
        super.onReload();

        if (game != null) {
            game.stopGame();
            game.getLogger().info("Reloading game...");
            game.startGame();
        }
    }

    @Override
    public void onDisable() {
        // We just need to shut down the current game
        if (game != null) {
            game.shutdown();
        }

        super.onDisable();
    }

    /**
     * Returns the game instance.
     *
     * @return The game instance
     */
    @Deprecated
    public final G getGame() {
        return game;
    }

    /**
     * Returns the current game instance.
     *
     * @return The game
     */
    @Nullable
    public G getCurrentGame() {
        return game;
    }

    /**
     * Registers a new map cycle constructor.
     *
     * @param name The map cycle name
     * @param constructor The constructor to use when creating a new map cycle instance
     */
    public void registerMapCycle(String name, MapCycleConstructor<M> constructor) {
        cycleConstructors.put(getConstructorName(name), constructor);
    }

    /**
     * Gets a map cycle constructor by its name.
     *
     * @param name The map cycle name
     * @return The found constructor, or null
     */
    @Nullable
    public MapCycleConstructor<M> getMapCycle(String name) {
        return cycleConstructors.get(getConstructorName(name));
    }

    /**
     * "Builds" a game configuration by merging the normal game config with the
     * custom settings in the map config.
     *
     * @param map The map this game is played in
     * @return The merged, built game configuration
     */
    protected ConfigurationSection buildGameConfiguration(M map) {
        if (map == null) {
            return getGameConfig();
        }

        return PluginConfig.mergeConfigurations(getGameConfig(), map.getGameConfig());
    }

    private ConfigurationSection buildMapConfiguration(ConfigurationSection mapConfig) {
        ConfigurationSection defaultMapConfig = getMapsConfig().getConfigurationSection(GameMap.KEY_DEFAULT_CONFIG);

        if (mapConfig == null) {
            if (defaultMapConfig == null) {
                return new MemoryConfiguration();

            } else {
                return defaultMapConfig;
            }
        }

        return PluginConfig.mergeConfigurations(defaultMapConfig, mapConfig);
    }

    protected void runNextGame() {
        // Just get the next map
        M nextMap = getNextMap();

        // Stop the server when we don't have a next map
        if (nextMap == null) {
            shutdown("No next map found");
            return;

        } else {
            getPluginLog().info("Creating new game for map '" + nextMap.getName() + '\'');
        }

        // Load next map
        if (getMapLoader() == null) {
            shutdown("No map loader instantiated");
            return;
        }

        if (!nextMap.load(getMapLoader())) {
            getPluginLog().info("Map didn't get loaded properly, will use next map.");
            runNextGame();
            return;
        }

        // ... or just continue with the next game!
        GameMap oldGameMap = this.game != null ? this.game.getMap() : null;
        this.game = createGame(nextMap, buildGameConfiguration(nextMap));

        if (game == null) {
            shutdown("Plugin did not return any game instance");
            return;
        }

        // Add all players
        game.onAddPlayers();

        // Unload old map if we had a game before
        if (oldGameMap != null) {
            getPluginLog().info("Unloading old map: " + oldGameMap.getDisplayName());

            if (oldGameMap.isLoaded()) {
                getServer().unloadWorld(oldGameMap.getWorld(), false);
            }
        }

        game.startGame();
    }

    private String getConstructorName(String name) {
        return name.toLowerCase();
    }

    private void shutdown(String message) {
        getPluginLog().warning(message + "! Shutting down the server...");
        getServer().shutdown();
    }

}
