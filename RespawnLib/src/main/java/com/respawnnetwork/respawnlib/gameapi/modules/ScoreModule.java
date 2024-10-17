package com.respawnnetwork.respawnlib.gameapi.modules;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerSetSpectatorEvent;
import com.respawnnetwork.respawnlib.network.scoreboard.Scoreboards;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;

/**
 * Represents a module that contains a score for the whole game,
 * or for each team (when the TeamModule was also added to the game).
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class ScoreModule<G extends Game> extends GameModule<G> implements Listener {
    public static final String SPECTATOR_TEAM = "spectators";

    private final Scoreboards scoreboards;

    private Team spectatorTeam;


    public ScoreModule(G game) {
        super(game);

        this.scoreboards = new Scoreboards();
    }

    @Override
    protected boolean onEnable() {
        // Create general score objective
        getScoreboards().createObjective(getName());
        getScoreboards().displayName(getConfig().getString("name", getDisplayName()));
        getScoreboards().displaySlot(DisplaySlot.SIDEBAR);

        // Create spectator team
        spectatorTeam = getScoreboards().getScoreboard().getTeam(SPECTATOR_TEAM);

        if (spectatorTeam == null) {
            spectatorTeam = getScoreboards().getScoreboard().registerNewTeam(SPECTATOR_TEAM);
        }

        spectatorTeam.setPrefix(ChatColor.GRAY.toString() + ChatColor.ITALIC);
        spectatorTeam.setSuffix(ChatColor.RESET.toString());

        return true;
    }

    @Override
    protected void onDisable() {
        getScoreboards().removeFromAll();
    }

    @EventHandler
    public void onSpectatorChange(PlayerSetSpectatorEvent event) {
        Player player = event.getGamePlayer().getPlayer();
        if (spectatorTeam == null || player == null) {
            return;
        }

        // Add or remove player
        if (event.enableSpectator()) {
            spectatorTeam.addPlayer(player);

        } else {
            spectatorTeam.removePlayer(player);
        }
    }

    public Score setScore(String name, int value) {
        return getScoreboards().score(name, value);
    }

    public Scoreboards getScoreboards() {
        return scoreboards;
    }

    @Override
    public String getDisplayName() {
        return "Score";
    }

    @Override
    public String getName() {
        return "score";
    }

}
