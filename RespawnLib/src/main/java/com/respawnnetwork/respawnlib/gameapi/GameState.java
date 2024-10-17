package com.respawnnetwork.respawnlib.gameapi;

/**
 * Represents a game state.
 * <p />
 * Game states can be switched however you like it.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public abstract class GameState<G extends Game> extends GameExtension<G> {

    /**
     * Creates a new game state instance.
     *
     * @param game The game that we're running
     */
    protected GameState(G game) {
        super(game, "game state", "{%s}");
    }

    /**
     * Subclasses should override this method which gets executed whenever this game state gets loaded.
     * <p />
     * This usually happens on a game start. This will not get called when the start of the game has been
     * cancelled.
     */
    protected boolean onLoad() {
        // Nothing to do ...
        return true;
    }

    /**
     * Subclasses should override this method which gets executed whenever this game state gets activated.
     */
    protected void onEnter() {
        // Nothing to do ...
    }

    /**
     * Subclasses should override this method which gets executed whenever this game state gets deactivated.
     */
    protected void onLeave() {
        // Nothing to do ...
    }

}
