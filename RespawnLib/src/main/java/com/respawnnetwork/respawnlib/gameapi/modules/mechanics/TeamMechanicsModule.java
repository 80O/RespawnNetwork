package com.respawnnetwork.respawnlib.gameapi.modules.mechanics;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerJoinGameEvent;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an extension to the player mechanics that adds functionality
 * for teams.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class TeamMechanicsModule<G extends Game> extends PlayerMechanicsModule<G> {
    @Getter
    private TeamModule<G> teamModule;


    public TeamMechanicsModule(G game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected boolean onEnable() {
        GameModule module = getGame().getModule(TeamModule.class);

        if (module == null || !module.isLoaded()) {
            getLogger().warning("Team module does not exist or is not loaded! Please rearrange load order!");

        } else if(module instanceof TeamModule) {
            teamModule = (TeamModule) module;
            getLogger().info("Team module found and hooked, will respawn players at team spawn and reset team inv.");
        }

        return super.onEnable();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinGameEvent event) {
        if (getTeamModule() == null || !(getGame().getCurrentState() instanceof InGameState || getGame().getCurrentState() instanceof CountdownState)) {
            return;
        }

        GamePlayer gamePlayer = event.getGamePlayer();

        // Check if the player is already in a team, if he is, do nothing
        if (getTeamModule().getTeam(gamePlayer) != null) {
            return;
        }

        // Check if we have space
        if (getTeamModule().getTeams().size() < 1) {
            return;
        }

        List<Team> sortedList = new LinkedList<>(teamModule.getTeams());
        Collections.sort(sortedList, new Comparator<Team>() {
            @Override
            public int compare(Team a, Team b) {
                return a.getPlayers().size() - b.getPlayers().size();
            }
        });

        // Add player to team

        Team team = sortedList.get(0);
        if (teamModule.addPlayerToTeam(gamePlayer, team)) {
            onResetPlayer(gamePlayer, team);
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (getTeamModule() == null) {
            return;
        }

        Player player = event.getPlayer();

        // Custom special FX - *boom* *pang* *pow* *whoosh*
        player.playSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 1.5f, 1.125f);

        GamePlayer gamePlayer = getGame().getPlayer(player);
        if (gamePlayer == null) {
            return;
        }

        Team team = getTeamModule().getTeam(gamePlayer);
        if (team != null) {
            onResetPlayer(gamePlayer, team);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerKickCheckTeams(PlayerKickEvent event) {
        checkTeams(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerKickCheckTeams(PlayerQuitEvent event) {
        checkTeams(event.getPlayer());
    }

    @SuppressWarnings("unchecked")
    private void checkTeams(Player player) {
        GamePlayer gamePlayer = getGame().getPlayer(player);
        if (gamePlayer != null) {
            getGame().removePlayer(gamePlayer);
        }

        int teamsValid = 0;

        for (Team team : teamModule.getTeams()) {
            teamModule.removePlayerFromTeam(gamePlayer, team);
            team.getPlayers().remove(gamePlayer);

            if (team.getPlayers().size() < team.getMinPlayers()) {
                continue;
            }

            teamsValid++;
        }

        // Go to next state
        if (teamsValid < 2) {
            getLogger().info("No more players left, stopping game");
            getGame().stopGame();
        }
    }

    @Override
    protected void onPlayerRespawns(GamePlayer gamePlayer, PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Don't handle respawns if we don't have the team module
        if (getTeamModule() != null) {
            Team team = getTeamModule().getTeam(gamePlayer);

            // Respawn player at his team spawn
            if (team != null) {
                respawnPlayer(gamePlayer, team.getSpawnLocation());
                return;
            }

            getLogger().warning("Could not find team for player " + player.getName());
        }

        super.onPlayerRespawns(gamePlayer, event);
    }

    /**
     * Resets the given player.
     *
     * @param player The player to reset
     * @param team   The team the player is in
     */
    protected void onResetPlayer(final GamePlayer player, final Team team) {
        Bukkit.getScheduler().runTaskLater(getGame().getPlugin(), new Runnable() {
            @Override
            public void run() {
                getLogger().info("Resetting inventory for player " + player.getName() + " in team " + team.getDisplayName());
                team.resetInventory(player.getPlayer());
            }
         }, 1L);
    }

    @Override
    public String getDisplayName() {
        return "Team Player Mechanics";
    }

}
