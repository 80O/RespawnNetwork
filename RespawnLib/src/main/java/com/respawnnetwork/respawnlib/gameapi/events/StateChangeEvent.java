package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.bukkit.events.CancellableEvent;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameState;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets executed whenever a game state changes.
 * <p />
 * This event is cancellable.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class StateChangeEvent extends CancellableEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final GameState previous;
    private final GameState next;


    public StateChangeEvent(Game game, GameState previous, GameState next) {
        this(false, game, previous, next);
    }

    public StateChangeEvent(boolean isAsync, Game game, GameState previous, GameState next) {
        super(isAsync);
        this.game = game;
        this.previous = previous;
        this.next = next;
    }

    /**
     * Returns the newly started game.
     *
     * @return The game object
     */
    public Game getGame() {
        return game;
    }

    /**
     * Gets the previous state.
     * <p />
     * This might return null if the game has just started.
     *
     * @return The previous game state, or null
     */
    public GameState getPrevious() {
        return previous;
    }

    /**
     * Gets the next state.
     * <p />
     * This might return null if the game has almost finished.
     *
     * @return The next game state, or null
     */
    public GameState getNext() {
        return next;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
