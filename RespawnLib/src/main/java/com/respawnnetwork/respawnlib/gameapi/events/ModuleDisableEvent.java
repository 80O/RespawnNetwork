package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets executed whenever a game module is about to be disabled.
 * <p />
 * This event is not cancellable.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class ModuleDisableEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final GameModule module;


    public ModuleDisableEvent(Game game, GameModule module) {
        this(false, game, module);
    }

    public ModuleDisableEvent(boolean isAsync, Game game, GameModule module) {
        super(isAsync);

        this.game = game;
        this.module = module;
    }

    /**
     * Returns the game that contains this module.
     *
     * @return The game object
     */
    public Game getGame() {
        return game;
    }

    /**
     * Gets the module that's about to be disabled.
     *
     * @return The game module
     */
    public GameModule getModule() {
        return module;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
