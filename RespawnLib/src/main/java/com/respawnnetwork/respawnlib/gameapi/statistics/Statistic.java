package com.respawnnetwork.respawnlib.gameapi.statistics;

import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.lang.Nameable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents a trackable statistic.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 * @see com.respawnnetwork.respawnlib.gameapi.statistics.PlayerStatistics
 */
@AllArgsConstructor
public class Statistic implements Nameable, Displayable {
    /** The list of default statistics */
    private static final List<Statistic> DEFAULTS = new LinkedList<>();

    /** The default namespace for statistics from the library */
    public static final String DEFAULT_NAMESPACE = "lib";

    /** The player deaths statistic */
    public static final Statistic DEATHS = create("deaths", "Deaths");

    /** The player kills statistic */
    public static final Statistic KILLS = create("kills", "Kills");

    /** The shots fired (arrows) statistic */
    public static final Statistic SHOTS_FIRED = create("shotsFired", "Shots fired");

    /** The number of shots that hit */
    public static final Statistic SHOTS_HIT = create("shotsHit", "Shots that hit");

    /** The number of shots taken */
    public static final Statistic SHOTS_TAKEN = create("shotsTaken", "Shots taken");

    /** The damage dealt statistic */
    public static final Statistic DAMAGE_DEALT = create("damageDealt", "Damage Dealt");

    /** The damage taken statistic */
    public static final Statistic DAMAGE_TAKEN = create("damageTaken", "Damage Taken");

    @Getter
    private final String namespace;

    @Getter
    private final String name;

    @Getter
    private final String displayName;

    @Getter
    private final double defaultValue;


    /**
     * Returns the identifiable name for this statistic.
     *
     * @return The statistic's identifier
     */
    public final String getIdentifier() {
        return getNamespace() + ':' + getName();
    }

    /**
     * Returns a list of default statistics.
     *
     * @return An unmodifiable collection of default statistics
     */
    public static Collection<Statistic> getDefaults() {
        return Collections.unmodifiableCollection(DEFAULTS);
    }

    private static Statistic create(String name, String displayName) {
        Statistic statistic = new Statistic(DEFAULT_NAMESPACE, name, displayName, 0);
        DEFAULTS.add(statistic);

        return statistic;
    }

}
