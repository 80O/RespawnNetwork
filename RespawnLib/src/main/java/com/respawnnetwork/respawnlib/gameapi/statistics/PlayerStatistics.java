package com.respawnnetwork.respawnlib.gameapi.statistics;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import gnu.trove.map.hash.THashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Represents a container for player statistics.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@AllArgsConstructor
public final class PlayerStatistics {
    private static final double DEFAULT_VALUE = -1;

    /** The game statistics that created this player statistics instance */
    @Getter
    private final GameStatistics gameStatistics;

    /** The map holding all statistics */
    private final Map<Statistic, Entry> statistics = new THashMap<>();


    /**
     * Gets a certain statistic value.
     *
     * @param statistic The statistic to get
     * @return The saved value, the statistic's default value or -1 if the given statistic is nil
     */
    public double get(Statistic statistic) {
        return get(statistic, null);
    }

    /**
     * Gets the statistic value for the game player.
     * <p />
     * If the given player is a nil, this returns the default value.
     *
     * @param statistic The statistic to get
     * @param gamePlayer The player to get the statistic for
     * @return The statistic's value or -1 if the given statistic is out of range
     */
    public double get(Statistic statistic, @Nullable GamePlayer gamePlayer) {
        if (statistic == null) {
            return DEFAULT_VALUE;
        }

        Entry entry = statistics.get(statistic);

        if (entry != null) {
            if (gamePlayer != null) {
                Double val = entry.perPlayer.get(gamePlayer);

                if (val != null) {
                    return val;
                }

            } else {
                return entry.value;
            }
        }

        return statistic.getDefaultValue();
    }

    /**
     * Increases a statistic.
     *
     * @param statistic The statistic to increase
     * @return The increased statistic (new value)
     */
    public double increase(Statistic statistic) {
        return increase(statistic, 1);
    }

    /**
     * Increases a statistic.
     *
     * @param statistic The statistic to increase
     * @param amount The amount to increase
     * @return The increased statistic (new value)
     */
    public double increase(Statistic statistic, double amount) {
        return increase(statistic, null, amount);
    }

    /**
     * Increases the statistic for the game player.
     * <p />
     * This also increases the normal, overall statistic.
     *
     * @param statistic The statistic to increase
     * @param gamePlayer The player to increase the statistic for
     * @return The increased statistic (new value)
     */
    public double increase(Statistic statistic, @Nullable GamePlayer gamePlayer) {
        return increase(statistic, gamePlayer, 1);
    }

    /**
     * Increases the statistic for the game player.
     * <p />
     * This also increases the normal, overall statistic.
     *
     * @param statistic The statistic to increase
     * @param gamePlayer The player to increase the statistic for
     * @param amount The amount to increase
     * @return The increased statistic (new value)
     */
    public double increase(Statistic statistic, @Nullable GamePlayer gamePlayer, double amount) {
        if (statistic == null) {
            return DEFAULT_VALUE;
        }

        Entry entry = statistics.get(statistic);

        // If not present, create new entry
        if (entry == null) {
            entry = new Entry();
            entry.value = statistic.getDefaultValue();

            statistics.put(statistic, entry);
        }

        // Increase value for specific player
        if (gamePlayer != null) {
            Double val = entry.perPlayer.get(gamePlayer);

            if (val != null) {
                double v = val + amount;

                entry.perPlayer.put(gamePlayer, v);
            }
        }

        // Increase default value
        entry.value += amount;

        return entry.value;
    }

    /**
     * Resets all the stored statistics.
     */
    public void reset() {
        statistics.clear();
    }


    /**
     * A wrapper for all statistic entries
     */
    private static class Entry {
        private Map<GamePlayer, Double> perPlayer = new THashMap<>();
        private double value;
    }

}
