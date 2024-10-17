package com.respawnnetwork.respawnlib.gameapi;

/**
 * A module that adds game functionality.
 * <p />
 * Game modules extend the normal functionality of a game by adding configurable features.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public abstract class GameModule<G extends Game> extends GameExtension<G> {

    /**
     * Creates a new game module instance.
     *
     * @param game The game instance
     */
    protected GameModule(G game) {
        super(game, "module", "[%s]");
    }

    /**
     * Subclasses should override this method which gets executed whenever this module gets enabled.
     */
    protected boolean onEnable() {
        // Nothing ...

        return true;
    }

    /**
     * Subclasses should override this method which gets executed whenever this module gets disabled.
     */
    protected void onDisable() {
        // Nothing ...
    }

    /**
     * Indicates whether or not this module should be automatically enabled when
     * a game starts.
     *
     * @return True if should be automatically enabled
     */
    protected boolean autoEnable() {
        return true;
    }

}
