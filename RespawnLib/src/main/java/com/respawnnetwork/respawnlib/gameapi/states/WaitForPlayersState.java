package com.respawnnetwork.respawnlib.gameapi.states;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.events.PlayerJoinGameEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Represents a state that is waiting for a certain amount of players to join the game.
 * When the needed amount is reached, this will automatically go to the next state.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public abstract class WaitForPlayersState<G extends Game> extends IdleGameState<G> implements Listener {
    private int amountNeeded;


    public WaitForPlayersState(G game) {
        super(game);
    }

    @Override
    protected void onEnter() {
        super.onEnter();

        this.amountNeeded = getAmountNeeded();

        // Add all players that are online
        for (Player player : getGame().getPlugin().getServer().getOnlinePlayers()) {
            handlePlayer(getGame().addPlayer(player));
        }

        checkPlayers();
    }

    /**
     * Gets the amount needed to get the next state.
     *
     * @return The needed amount of players
     */
    protected abstract int getAmountNeeded();

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinGameEvent event) {
        // Just in case ...
        GamePlayer gamePlayer = event.getGamePlayer();
        if (!gamePlayer.getGame().equals(getGame())) {
            return;
        }

        // Handle player and check if we can go to the next state
        handlePlayer(gamePlayer);
        checkPlayers();
    }

    /**
     * Handles a newly joined game player.
     * <p />
     * This usually clears his inventory, teleports him to the spawn location of the map and sets him to spectator
     *
     * @param player The player to handle
     */
    protected void handlePlayer(GamePlayer player) {
        // Set to spectator and teleport to spawn location
        player.clearInventory();
        player.teleportTo(getGame().getMap().getSpawnLocation());
        player.setSpectator(true);
    }

    /**
     * Updates the scoreboard whenever a player left or joined.
     */
    protected void updateScoreboard() {
        getScoreboards().score("Players needed:", amountNeeded - getGame().getNumberOfPlayers());
    }

    private void checkPlayers() {
        updateScoreboard();

        if (getGame().getNumberOfPlayers() >= amountNeeded) {
            getGame().nextState();
        }
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onPlayerLeave(PlayerQuitEvent event) {
        GamePlayer gamePlayer = getGame().getPlayer(event.getPlayer());

        if (gamePlayer != null) {
            getGame().removePlayer(gamePlayer);
        }

        checkPlayers();
    }

    @Override
    public String getDisplayName() {
        return "Waiting";
    }

}
