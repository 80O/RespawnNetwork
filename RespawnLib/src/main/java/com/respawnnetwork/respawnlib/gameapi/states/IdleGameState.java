package com.respawnnetwork.respawnlib.gameapi.states;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameState;
import com.respawnnetwork.respawnlib.network.scoreboard.Scoreboards;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;

/**
 * Represents an idle game state.
 * <p />
 * This state can only be skipped trough other stuff, this will not go to
 * the next state on its own!
 *
 * @author spaceemotion
 * @version 1.0
 * @version 1.0.1
 */
public class IdleGameState<G extends Game> extends GameState<G> {
    @Getter(AccessLevel.PROTECTED)
    private Scoreboards scoreboards;


    public IdleGameState(G game) {
        super(game);
    }

    @Override
    protected boolean onLoad() {
        scoreboards = new Scoreboards();
        scoreboards.createObjective("waiting");
        scoreboards.displayName(ChatColor.BOLD + "Waiting for players");

        return true;
    }

    @Override
    protected void onEnter() {
        scoreboards.assignToAll();
        scoreboards.displaySlot(DisplaySlot.SIDEBAR);
    }

    @Override
    protected void onLeave() {
        scoreboards.displaySlot(null);
    }

    @Override
    public String getDisplayName() {
        return "Idle";
    }

    @Override
    public String getName() {
        return "idle";
    }

}
