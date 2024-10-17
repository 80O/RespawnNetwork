package com.respawnnetwork.respawnlib.gameapi.states;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameState;

/**
 * Represents a basic prepare game state.
 *
 * @author spaceemotion
 * @version 1.0
 */
public abstract class PrepareGameState<G extends Game> extends GameState<G> {

    protected PrepareGameState(G game) {
        super(game);
    }

    @Override
    public String getName() {
        return "PREPARE";
    }

    @Override
    public String getDisplayName() {
        return "Preparing Game";
    }

}
