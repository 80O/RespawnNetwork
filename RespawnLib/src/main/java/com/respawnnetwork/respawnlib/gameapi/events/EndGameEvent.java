package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.gameapi.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets executed whenever a game has ended.
 * <p />
 * This event is not cancellable.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class EndGameEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final boolean forced;


    public EndGameEvent(Game game, boolean forced) {
        this(false, game, forced);
    }

    public EndGameEvent(boolean isAsync, Game game, boolean forced) {
        super(isAsync);

        this.game = game;
        this.forced = forced;
    }

    /**
     * Returns the game that has ended.
     *
     * @return The game object
     */
    public Game getGame() {
        return game;
    }

    /**
     * Indicates whether or not the game has forcefully been ended.
     *
     * @return True on a forceful end, false on a normal one
     */
    public boolean isForced() {
        return forced;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
