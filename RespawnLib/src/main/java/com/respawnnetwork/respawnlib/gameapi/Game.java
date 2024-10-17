package com.respawnnetwork.respawnlib.gameapi;

import com.respawnnetwork.respawnlib.gameapi.events.*;
import com.respawnnetwork.respawnlib.gameapi.maps.GameMap;
import com.respawnnetwork.respawnlib.gameapi.statistics.GameStatistics;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.utils.StringUtils;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Represents a basic game.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public abstract class Game<G extends GamePlayer, P extends GamePlugin, M extends GameMap> {
    /** The default game configuration file name, including the file suffix */
    public static final String CONFIG_FILE_NAME = "game.yml";

    /** the plugin that runs this game */
    @Getter
    private final P plugin;

    /** The game map */
    @Getter
    private final M map;

    /** The game configuration for this game and map only */
    @Getter
    private final ConfigurationSection config;

    /** A list containing all game states in the order they've been added */
    private final List<GameState> gameStateList;

    /** A list holding the modules in the order in which they've been added */
    private final List<GameModule> gameModuleList;

    /** A map holding all game modules by their classes */
    private final Map<Class<? extends GameState>, GameState> gameStates;

    /** A map holding all game states by their classes */
    private final Map<Class<? extends GameModule>, GameModule> gameModules;

    /** Holds the conversion from bukkit uuid to game players */
    private final Map<UUID, G> players;

    /** The list of spectators in the game */
    @Getter
    private final List<G> spectators;

    /** A list of all real players (holding no spectators) */
    private final List<G> realPlayers;

    /** The logger for the game log files */
    @Getter
    private final Logger logger;

    /** The game descriptor "file" */
    @Getter
    private final GameDescriptor descriptor;

    /** The game statistics */
    @Getter
    private final GameStatistics statistics;
    private final GameEventListener gameEventListener;

    /** An iterator for our game states */
    private ListIterator<GameState> stateIterator;

    /** The current state */
    private GameState currentState;


    /**
     * Creates a new game instance.
     *
     * @param plugin The plugin that created this game
     * @param map The map this game runs in
     * @param config The configuration for this game
     */
    public Game(P plugin, M map, ConfigurationSection config) {
        this.plugin = plugin;
        this.map = map;
        this.config = config;

        // Set up maps and lists
        this.gameStateList = new LinkedList<>();
        this.gameModuleList = new LinkedList<>();
        this.gameStates = new THashMap<>();
        this.gameModules = new THashMap<>();
        this.players = new THashMap<>();
        this.spectators = new LinkedList<>();
        this.realPlayers = new LinkedList<>();

        // .. as well as the other stuff
        this.logger = new GameLogger(this);
        this.descriptor = new GameDescriptor(getLogger(), getConfig().getConfigurationSection("game"));
        this.statistics = new GameStatistics(this);

        // Create general listener
        gameEventListener = new GameEventListener(this);

        // Log some information
        logger.info("Game with name " + getDescriptor().getName() + " created. Have fun!");
    }

    /**
     * Creates a new message instance to use for game-related messages.
     *
     * @return The newly created message instance
     */
    public Message createMessage() {
        return new Message("&l[&6" + getDescriptor().getName() + "&r&l] &r%s");
    }

    /**
     * Adds all the necessary game states.
     */
    protected abstract void addStates();

    /**
     * Adds all the game modules.
     */
    protected abstract void addModules();

    /**
     * Adds players to the game.
     * <p />
     * Defaults to all players on the server.
     *
     * @since 1.0.1
     */
    protected void onAddPlayers() {
        // Add all players
        for (Player player : getPlugin().getServer().getOnlinePlayers()) {
            addPlayer(player).teleportTo(getMap().getSpawnLocation());
        }
    }

    /**
     * Subclasses should override this method which gets executed whenever a new game starts.
     */
    protected void onStartGame() {
        plugin.registerListener(gameEventListener);

//        for (G gamePlayer : players.values()) {
//            gamePlayer.setSpectator(false);
//        }
    }

    /**
     * Subclasses should override this method which gets executed whenever a game has ended.
     *
     * @param forcedStop True if the ending has been forced or not
     * @see #stopGame()
     */
    protected void onEndGame(boolean forcedStop) {
//        for (G gamePlayer : players.values()) {
//            gamePlayer.setSpectator(true);
//        }

        HandlerList.unregisterAll(gameEventListener);
    }

    /**
     * Adds a new state to the game.
     * <p />
     * If a game has already started this will do nothing and just return the given state.
     *
     * @param gameState The state to add
     */
    public <T extends GameState> T addState(T gameState) {
        if (!isRunning()) {
            gameStates.put(gameState.getClass(), gameState);
            gameStateList.add(gameState);
        }

        return gameState;
    }

    public <T extends GameModule> T addModule(T module) {
        // Only add module when the game is not running and we hadn't added it previously
        if (!isRunning() && !gameModules.containsKey(module.getClass())) {
            gameModules.put(module.getClass(), module);
            gameModuleList.add(module);
        }

        return module;
    }

    /**
     * Gets the current state if the game is running.
     *
     * @return The current game state or null
     */
    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Sets the current state.
     * <p />
     * This might still get cancelled in a {@link com.respawnnetwork.respawnlib.gameapi.events.StateChangeEvent}.
     *
     * @param state The state to set
     * @return True if we were able to set the state
     */
    public boolean setState(Class<? extends GameState> state) {
        ListIterator<GameState> iterator = gameStateList.listIterator();
        GameState gameState;

        while (iterator.hasNext()) {
            gameState = iterator.next();

            // Check if we might have a winner
            if (!state.isAssignableFrom(state.getClass())) {
                continue;
            }

            // Call event first
            StateChangeEvent stateChangeEvent = new StateChangeEvent(this, currentState, gameState);
            callEvent(stateChangeEvent);

            if (stateChangeEvent.isCancelled()) {
                doStateChange(currentState, gameState, true);

            } else {
                doStateChange(currentState, gameState, false);

                // Set everything
                stateIterator = iterator;
                currentState = gameState;

                return true;
            }
        }

        return false;
    }

    /**
     * Switches to the next game state.
     *
     * @return True if we were able to go to the next state
     */
    public boolean nextState() {
        // Hold on, we're not in a game yet!
        if (!isRunning()) {
            return false;
        }

        GameState nextState = null;

        if (stateIterator.hasNext()) {
            // Enter the next state
            nextState = stateIterator.next();
        }

        // Call events
        StateChangeEvent stateChangeEvent = new StateChangeEvent(this, currentState, nextState);
        callEvent(stateChangeEvent);

        if (stateChangeEvent.isCancelled()) {
            doStateChange(currentState, nextState, true);

            // Go back in the iterator when the event got cancelled
            if (stateIterator.hasPrevious()) {
                stateIterator.previous();
            }

            return false;

        } else {
            GameState previous = currentState;
            currentState = nextState;

            doStateChange(previous, nextState, false);

            // We hit the end of the list, end the game
            if (currentState == null) {
                endGame(false);
            }

            return true;
        }
    }

    /**
     * Gets a module by its class.
     *
     * @param moduleClass The class of the module
     * @param <T> The module type
     * @return The found module, or null if it could not be found
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends GameModule> T getModule(Class<T> moduleClass) {
        GameModule module = gameModules.get(moduleClass);

        if (module != null && module.getClass().isAssignableFrom(moduleClass)) {
            return (T) module;
        }

        return null;
    }

    /**
     * Gets a state by its class.
     *
     * @param stateClass The class of the state
     * @param <T> The state type
     * @return The found game state, or null if it could not be found
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends GameState> T getState(Class<T> stateClass) {
        GameState state = gameStates.get(stateClass);

        if (state != null && stateClass.isAssignableFrom(state.getClass())) {
            return (T) state;
        }

        return null;
    }

    /**
     * Enables a module.
     *
     * @param moduleClass The class of the module
     */
    public void enableModule(Class<? extends GameModule> moduleClass) {
        GameModule module = getModule(moduleClass);

        // Do nothing if the module does not exist
        if (module == null) {
            return;
        }

        enableModule(module);
    }

    public void enableModule(@NotNull GameModule module) {
        logger.info("Enabling module '" + module.getDisplayName() + '\'');

        // Call event
        ModuleEnableEvent moduleEnableEvent = new ModuleEnableEvent(this, module);
        callEvent(moduleEnableEvent);

        if (moduleEnableEvent.isCancelled()) {
            logger.info("Enabling of module '" + module.getDisplayName() + "'got cancelled, will not enable!");
            return;
        }

        boolean success = module.onEnable();

        if (!success) {
            logger.warning("Module '" + module.getDisplayName() + "' did not load successfully, module will be unloaded!");

        } else {
            // Only register as listener when we successfully loaded the module
            if (module instanceof Listener) {
                module.registerListener((Listener) module);
            }
        }

        module.setLoaded(success);
    }

    /**
     * Disables a module.
     *
     * @param moduleClass The class of the module
     */
    public void disableModule(Class<? extends GameModule> moduleClass) {
        GameModule module = getModule(moduleClass);

        // Do nothing if the module does not exist
        if (module == null) {
            return;
        }

        disableModule(module);
    }

    /**
     * Disables the specified module.
     * <p />
     * This will only disable the module, it will not remove it from the game itself.
     *
     * @param module The module to disable
     */
    public void disableModule(@NotNull GameModule module) {
        logger.info("Disabling module '" + module.getDisplayName() + '\'');

        // Call event
        callEvent(new ModuleDisableEvent(this, module));

        module.onDisable();
        module.setLoaded(false);

        // Most important thing: remove all listeners, so no old references are kept
        module.unregisterListeners();

        // Also un-register listener modules
        if (module instanceof Listener) {
            HandlerList.unregisterAll((Listener) module);
        }
    }

    /**
     * Creates a new game player instance.
     *
     * @param player The bukkit player
     * @return The new game player
     */
    @NotNull
    public abstract G createPlayer(Player player);

    protected abstract G[] convertPlayerArray(Collection<G> collection);

    /**
     * Gets a game player by its bukkit version.
     *
     * @param player The bukkit player
     * @return The game player, or null if it could not be found
     */
    @Nullable
    public G getPlayer(Player player) {
        if (player == null) {
            return null;
        }

        return players.get(player.getUniqueId());
    }

    /**
     * Checks if a player is in the game.
     *
     * @param player The bukkit player
     * @return True if the player is in the game, false if not
     * @since 1.0.1
     */
    public boolean isPlaying(Player player) {
        return player != null && players.containsKey(player.getUniqueId());
    }

    /**
     * Adds a player to this game.
     *
     * @param player The bukkit player
     * @return The new GamePlayer instance
     */
    @NotNull
    public G addPlayer(Player player) {
        G gamePlayer = getPlayer(player);
        if (gamePlayer != null) {
            return gamePlayer;
        }

        return addPlayer(createPlayer(player));
    }

    @NotNull
    public G addPlayer(G gamePlayer) {
        // Add to player map
        Player player = gamePlayer.getPlayer();

        if (player != null) {
            players.put(player.getUniqueId(), gamePlayer);

            List<String> creators = getMap().getCreators();
            Message.INFO.provide("map", getMap().getDisplayName())
                    .provide("creators", creators.isEmpty() ? "the community" : StringUtils.implodeProperEnglish(creators))
                    .sendKey(player, "game.map-playing");
        }

        return gamePlayer;
    }

    /**
     * Adds a collection of players to this game.
     *
     * @param players The players to add
     */
    public void addPlayers(Collection<Player> players) {
        for (Player player : players) {
            addPlayer(player);
        }
    }

    /**
     * Returns a collection of all players that are in this game.
     *
     * @return The collection of players, including spectators
     * @see #addPlayer(org.bukkit.entity.Player)
     * @see #addPlayers(java.util.Collection)
     */
    @NotNull
    public G[] getPlayers() {
        return convertPlayerArray(players.values());
    }

    /**
     * Returns an array of all players that are no spectators, but in the game.
     *
     * @return The collection of real, participating players
     * @since 1.0.1
     */
    public G[] getRealPlayers() {
        return convertPlayerArray(realPlayers);
    }

    /**
     * Returns the number of all players in this game.
     *
     * @return The number of players including spectators
     */
    public int getNumberOfPlayers() {
        return players.size();
    }

    /**
     * Returns the number of actual players in this game.
     *
     * @return The number of players excluding spectators
     * @since 1.0.1
     */
    public int getNumberOfRealPlayers() {
        return players.size() - spectators.size();
    }

    /**
     * Returns an iterator for all game players.
     * <p />
     * Faster than using the {@link #getPlayers()} method.
     *
     * @return An iterator
     */
    public Iterator<G> getPlayerIterator() {
        return players.values().iterator();
    }

    /**
     * Removes a player from the game.
     *
     * @param gamePlayer The player to remove
     */
    public void removePlayer(G gamePlayer) {
        if (gamePlayer == null) {
            return;
        }

        Player player = gamePlayer.getPlayer();

        if (player != null) {
            players.remove(player.getUniqueId());
        }

        realPlayers.remove(gamePlayer);
        spectators.remove(gamePlayer);
    }

    /**
     * Checks if a player is a spectator by using the bukkit player instance.
     *
     * @param player The bukkit player
     * @return True if the player is a spectator, false if not
     * @since 1.0.1
     */
    public boolean isSpectator(Player player) {
        if (player == null) {
            return false;
        }

        for (G gamePlayer : getSpectators()) {
            if (!player.equals(gamePlayer.getPlayer())) {
                continue;
            }

            return true;
        }

        return false;
    }

    /**
     * @since 1.0.1
     */
    boolean addSpectator(G player) {
        if (player == null || !players.containsValue(player)) {
            return false;
        }

        if (!spectators.contains(player)) {
            spectators.add(player);
        }

        realPlayers.remove(player);
        callEvent(new PlayerSetSpectatorEvent(player, true));
        setSpectator(player.getPlayer(), true);

        return true;
    }

    /**
     * @since 1.0.1
     */
    boolean removeSpectator(G player) {
        if (player == null || !players.containsValue(player)) {
            return false;
        }

        if (!realPlayers.contains(player)) {
            realPlayers.add(player);
        }

        spectators.remove(player);
        callEvent(new PlayerSetSpectatorEvent(player, false));
        setSpectator(player.getPlayer(), false);

        return true;
    }

    /**
     * @since 1.0.1
     */
    protected void setSpectator(Player player, boolean enable) {
        if (player == null) {
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);

        player.setAllowFlight(enable);
        player.setFlying(enable);
    }

    /**
     * Starts the game.
     * <p />
     * This does nothing if we're already running a game.
     */
    public void startGame() {
        if (isRunning()) {
            return;
        }

        // Add all players

        // SpaceEmotion: moved this to game plugin
        // onAddPlayers();

        getLogger().info("Starting up the game");

        // Add states and check if we even can run a game
        addStates();

        if (gameStateList.isEmpty()) {
            getLogger().info("Cannot run a game without any states!");

            shutdown();
            return;
        }

        // Add modules
        addModules();

        // Call start game event
        StartGameEvent startGameEvent = new StartGameEvent(this);
        callEvent(startGameEvent);

        if (startGameEvent.isCancelled()) {
            getLogger().info("The start game event has been cancelled, aborting game");
            return;
        }

        // Load the game states
        Iterator<GameState> gameStateIterator = gameStateList.iterator();
        while (gameStateIterator.hasNext()) {
            GameState gameState = gameStateIterator.next();
            getLogger().info("Loading state '" + gameState.getDisplayName() + "'");

            if (!gameState.onLoad()) {
                getLogger().warning("Error loading state, will remove from state list");
                gameStateIterator.remove();
            }
        }

        // Enable all modules
        for(GameModule module : gameModuleList) {
            if (!module.autoEnable()) {
                continue;
            }

            enableModule(module);
        }

        // Reset current state
        stateIterator = gameStateList.listIterator();

        // Call the start game function and go to first state
        onStartGame();
        nextState();
    }

    /**
     * Forcefully stops the game.
     * <p />
     * This does nothing if we're not running a game.
     */
    public void stopGame() {
        if (!isRunning()) {
            return;
        }

        endGame(true);
    }

    public void shutdown() {
        if (!isRunning()) {
            return;
        }

        getLogger().info("Shutting down the game completely...");
        endGame(true);
    }

    /**
     * Indicates whether or not this game is running.
     * <p />
     * This does not include idle game states. This will only check if
     * we do not have a current state.
     *
     * @return True if it's running, false if not
     */
    public boolean isRunning() {
        return stateIterator != null;
    }

    /**
     * Indicates whether or not the game has ended.
     *
     * @return True if the game has ended, false if it's still running or never ran once.
     * @since 1.0.1
     */
    public boolean hasEnded() {
        return !isRunning();
    }

    /**
     * Convenience method to call a bukkit event.
     *
     * @param event The event to call
     */
    public void callEvent(Event event) {
        getPlugin().getServer().getPluginManager().callEvent(event);
    }

    /**
     * Plays a sound effect to all game players.
     *
     * @param sound The sound effect to play
     * @param vol The sound volume
     * @param pitch The pitch of the sound
     * @since 1.0.1
     */
    public void playSound(Sound sound, float vol, float pitch) {
        if (sound == null || vol == 0) {
            return;
        }

        for (GamePlayer gamePlayer : getPlayers()) {
            Player player = gamePlayer.getPlayer();

            if (player == null) {
                continue;
            }

            player.playSound(player.getLocation(), sound, vol, pitch);
        }
    }

    @SuppressWarnings("unchecked")
    private void endGame(boolean forced) {
        if (!isRunning()) {
            return;
        }

        logger.info("Ending game " + (forced ? "forcefully" : "normally"));

        // Clear state stuff
        stateIterator = null;
        currentState = null;

        // Call end game event
        callEvent(new EndGameEvent(this, forced));

        // Disable all modules
        for(GameModule module : gameModuleList) {
            disableModule(module);
        }

        // Call the end game function
        onEndGame(forced);

        // Clear states and modules
        gameStates.clear();
        gameStateList.clear();
        gameModules.clear();
        gameModuleList.clear();

        // Clear game players
        players.clear();

        // Notify the plugin
        getPlugin().onGameEnd(this);
    }

    private void doStateChange(GameState previous, GameState next, boolean cancelled) {
        if (cancelled) {
            // Log cancellation information
            StringBuilder msg = new StringBuilder("Game state change got cancelled, ");

            if (previous != null) {
                msg.append("will keep being in '").append(previous.getDisplayName()).append("'!");

            } else {
                msg.append("won't be able to start game properly!");
            }

            logger.info(msg.toString());

        } else {
            // Properly leave the previous state
            if (previous != null) {
                logger.info("Leaving the '" + previous.getDisplayName() + "' state");
                previous.onLeave();

                // Unregister listeners
                previous.unregisterListeners();

                if (previous instanceof Listener) {
                    HandlerList.unregisterAll((Listener) previous);
                }
            }

            // Enter the next state (when we have one)
            if (next != null) {
                logger.info("Entering the '" + next.getDisplayName() + "' state");
                next.onEnter();

                // Register listener
                if (next instanceof Listener) {
                    getPlugin().registerListener((Listener) next);
                }
            }
        }
    }


    final static class GameLogger extends Logger {
        String prefix;

        /**
         * Creates a new GameLogger that extracts the name from a game.
         *
         * @param game A reference to the game
         */
        private GameLogger(Game game) {
            super(game.getClass().getCanonicalName(), null);

            String name = game.getMap().getName();
            this.prefix = '(' + name + ") ";

            setParent(game.getPlugin().getPluginLog("map-" + name, "maps"));
            setLevel(Level.ALL);
        }

        @Override
        public void log(LogRecord logRecord) {
            logRecord.setMessage(prefix + logRecord.getMessage());

            super.log(logRecord);
        }
    }


}
