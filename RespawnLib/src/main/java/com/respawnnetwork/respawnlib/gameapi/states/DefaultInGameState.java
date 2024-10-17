package com.respawnnetwork.respawnlib.gameapi.states;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameState;

/**
 * Represents a basic implementation of the in game state.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class DefaultInGameState<G extends Game> extends GameState<G> implements InGameState {

    public DefaultInGameState(G game) {
        super(game);
    }

    @Override
    protected void onEnter() {
        getGame().createMessage().sendKey("game.begin");
    }

    @Override
    public String getDisplayName() {
        return "In-Game";
    }

    @Override
    public String getName() {
        return "ingame";
    }

}
