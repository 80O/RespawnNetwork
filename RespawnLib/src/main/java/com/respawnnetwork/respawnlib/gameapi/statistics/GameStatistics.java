package com.respawnnetwork.respawnlib.gameapi.statistics;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents a container for game statistics.
 * <p />
 * With this, statistics can be registered, tracked and untracked.
 * Statistics are getting stored by player.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public final class GameStatistics {
    /** The game instance */
    @Getter
    private final Game game;

    /** The map holding all statistics, by the player object */
    private final Map<GamePlayer, PlayerStatistics> playerStatistics;

    /** The registry for all statistics */
    private final Map<String, Statistic> registered;

    /** The list of statistics we're actually tracking */
    @Getter
    private final Collection<Statistic> tracking;


    /**
     * Creates a new game statistics instance.
     *
     * @param game The game instance
     */
    public GameStatistics(Game game) {
        this.game = game;

        this.playerStatistics = new THashMap<>();
        this.registered = new THashMap<>();
        this.tracking = new LinkedList<>();

        // Register defaults
        for (Statistic statistic : Statistic.getDefaults()) {
            register(statistic);
        }
    }

    /**
     * Registers a new statistic.
     * <p />
     * Automatically adds it to the tracking statistics. If the given statistic
     * is already registered, this will do nothing.
     *
     * @param statistic The statistic to register
     */
    public void register(Statistic statistic) {
        if (registered.containsKey(statistic.getIdentifier())) {
            return;
        }

        registered.put(statistic.getIdentifier(), statistic);

        getGame().getLogger().info("Registered statistic " + statistic.getIdentifier());
    }

    /**
     * Enables tracking for the given statistic.
     * <p />
     * Automatically registers it, if the statistic hasn't been registered before.
     *
     * @param statistic The statistic to track
     */
    public void track(Statistic statistic) {
        if (!registered.containsValue(statistic)) {
            register(statistic);
            return;
        }

        tracking.add(statistic);
    }

    /**
     * Disables tracking for the given statistic.
     *
     * @param statistic The statistic to disable tracking for
     */
    public void untrack(Statistic statistic) {
        if (!registered.containsValue(statistic)) {
            return;
        }

        tracking.remove(statistic);
    }

    /**
     * Indicates whether or not we're tracking the given statistic.
     *
     * @param identifier The statistic identifier
     * @return True if we're tracking the given statistic, false if not
     */
    public boolean isTracking(String identifier) {
        return isTracking(getByIdentifier(identifier));
    }

    /**
     * Indicates whether or not we're tracking the given statistic.
     *
     * @param statistic The statistic to check
     * @return True if we're tracking the given statistic, false if not
     */
    public boolean isTracking(Statistic statistic) {
        // We don't have to check the registry here
        return tracking.contains(statistic);
    }

    /**
     * Indicates whether or not we're tracking all of the given statistics.
     *
     * @param statistics The statistics to check
     * @return True if we're tracking the given statistics, false if not
     */
    public boolean isTracking(Statistic... statistics) {
        for (Statistic statistic : statistics) {
            if (isTracking(statistic)) {
                continue;
            }

            return false;
        }

        return true;
    }

    /**
     * Gets a registered statistic by its identifier.
     *
     * @param identifier The statistic identifier
     * @return The found statistic, or null
     */
    @Nullable
    public Statistic getByIdentifier(String identifier) {
        return registered.get(identifier);
    }

    /**
     * Gets or creates a new player statistics object for the given player.
     *
     * @param player The game player to get the statistics for
     * @return The player statistics object
     */
    public PlayerStatistics getFor(GamePlayer player) {
        PlayerStatistics statistics = playerStatistics.get(player);

        if (statistics == null) {
            statistics = new PlayerStatistics(this);

            playerStatistics.put(player, statistics);
        }

        return statistics;
    }

}
