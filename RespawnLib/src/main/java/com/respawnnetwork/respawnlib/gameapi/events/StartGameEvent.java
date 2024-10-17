package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.bukkit.events.CancellableEvent;
import com.respawnnetwork.respawnlib.gameapi.Game;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets executed whenever a game has started.
 * <p />
 * This event gets executed <b>before</b> the modules are loading. When you
 * need to do stuff on a game start inside a module, use the
 * {@link com.respawnnetwork.respawnlib.gameapi.events.StateChangeEvent StateChangeEvent}.
 * <p />
 * This event is cancellable.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class StartGameEvent extends CancellableEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Game game;


    public StartGameEvent(Game game) {
        this(false, game);
    }

    public StartGameEvent(boolean isAsync, Game game) {
        super(isAsync);

        this.game = game;
    }

    /**
     * Returns the newly started game.
     *
     * @return The game object
     */
    public Game getGame() {
        return game;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
