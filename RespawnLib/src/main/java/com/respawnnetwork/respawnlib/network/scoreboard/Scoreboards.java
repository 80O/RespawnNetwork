package com.respawnnetwork.respawnlib.network.scoreboard;

import gnu.trove.map.hash.THashMap;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 *
 * @author TomShar
 * @author spaceemotion
 * @version 1.0.1
 */
@SuppressWarnings("deprecation")
public class Scoreboards {
    private static final Server server = Bukkit.getServer();
    private static final Scoreboard emptyScoreboard = server.getScoreboardManager().getNewScoreboard();

    private static final Scoreboard scoreboard = server.getScoreboardManager().getMainScoreboard();
    private Objective objective;


    /**
     * Creates a new objective on the scoreboard.
     *
     * @param name name of the objective, type will be 'dummy'.
     */
    public Objective createObjective(String name) {
        return createObjective(name, "dummy");
    }

    /**
     * Creates a new objective on the scoreboard
     *
     * @param name name of the objective.
     * @param type type of the objective.
     */
    public Objective createObjective(String name, String type) {
        Objective found = getScoreboard().getObjective(name);

        if (found != null) {
            this.objective = found;

        } else {
            this.objective = getScoreboard().registerNewObjective(name, type);
        }

        return objective;
    }

    /**
     * Sets the display name of the objective
     *
     * @param name the name you want to change it to.
     */
    public void displayName(String name) {
        Objective objective1 = getObjective();
        if (objective1 == null) {
            return;
        }

        objective1.setDisplayName(name);
    }

    /**
     * Sets the display slot of the objective
     *
     * @param slot where you want the objective to be shown
     * @see org.bukkit.scoreboard.DisplaySlot
     */
    public void displaySlot(DisplaySlot slot) {
        Objective objective1 = getObjective();
        if (objective1 == null) {
            return;
        }

        objective1.setDisplaySlot(slot);
    }

    /**
     * Add/update a score on the objective
     *
     * @param name name of the score to add/update.
     * @param value value to set the score to.
     * @return The score object
     */
    @Nullable
    public Score score(String name, int value) {
        Objective objective1 = getObjective();
        if (objective1 == null) {
            return null;
        }

        Score score = objective1.getScore(server.getOfflinePlayer(name));
        score.setScore(value);

        return score;
    }

    /**
     * @see #removeScores(org.bukkit.OfflinePlayer)
     * @param name The name of the offline player to remove the scores from
     */
    public void removeScores(String name) {
        removeScores(Bukkit.getOfflinePlayer(name));
    }


    /**
     * Removes all scores from the current objective.
     * <p />
     * This uses the workaround as used in the wonderful
     * <a href="https://github.com/aufdemrand/Denizen/blob/master/src/main/java/net/aufdemrand/denizen/utilities/ScoreboardHelper.java">Denizen plugin</a>.
     *
     * @param player The player to remove the scores from
     */
    public void removeScores(OfflinePlayer player) {
        Objective obj = getObjective();
        if (obj == null) {
            return;
        }

        // There is no method to remove a single score from an
        // objective, as confirmed here:
        // https://bukkit.atlassian.net/browse/BUKKIT-4014

        Map<String, Integer> scores = new THashMap<>();

        // Go through every score for this (real or fake) player
        // and put it in the scores if it doesn't belong to the
        // objective we want to remove the score from
        for (Score score : getScoreboard().getScores(player)) {
            if (score.getObjective().equals(obj) || score.getScore() == 0) {
                continue;
            }

            scores.put(score.getObjective().getName(), score.getScore());
        }

        // Remove all the scores for this (real or fake) player
        getScoreboard().resetScores(player);

        // Go through the scores and add back all the scores we saved
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            getScoreboard().getObjective(entry.getKey()).getScore(player).setScore(entry.getValue());
        }
    }

    /**
     * Give scoreboard to player.
     *
     * @param p player to give the scoreboard to.
     */
    public void assignTo(Player p) {
        p.setScoreboard(getScoreboard());
    }

    /**
     * Give everyone on the server the scoreboard.
     */
    public void assignToAll() {
        for(Player p : server.getOnlinePlayers()) {
            assignTo(p);
        }
    }

    /**
     * Removes scoreboard from player.
     *
     * @param p Player to remove scoreboard from.
     */
    public void removeFrom(Player p) {
        p.setScoreboard(emptyScoreboard);
    }

    /**
     * Removes scoreboard from all players on the server.
     */
    public void removeFromAll() {
        for(Player p : server.getOnlinePlayers()) {
            removeFrom(p);
        }
    }

    public void clear() {
        for (OfflinePlayer offlinePlayer : getScoreboard().getPlayers()) {
            removeScores(offlinePlayer);
        }

//        Objective oldObjective = getObjective();
//        if (oldObjective == null) {
//            return;
//        }
//
//
//        // Get properties of old objective
//        String name = oldObjective.getName();
//        String displayName = oldObjective.getDisplayName();
//        DisplaySlot displaySlot = oldObjective.getDisplaySlot();
//        String criteria = oldObjective.getCriteria();
//
//        // Unregister old objective
//        oldObjective.unregister();
//
//        // Create new objective and assign old properties
//        Objective newObjective = createObjective(name, criteria);
//        newObjective.setDisplayName(displayName);
//        newObjective.setDisplaySlot(displaySlot);
    }

    /**
     * Returns the bukkit scoreboard we're manipulating.
     *
     * @return The bukkit scoreboard
     * @since 1.0.1
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    /**
     * Returns the objective of this scoreboard.
     * <p />
     * This can return null if no objective has been created yet.
     *
     * @return The objective of this scoreboard
     * @since 1.0.1
     */
    @Nullable
    public Objective getObjective() {
        return objective;
    }

}
