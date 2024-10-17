package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.bukkit.events.CancellableEvent;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets executed whenever a game module is about to be enabled.
 * <p />
 * This event is cancellable.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class ModuleEnableEvent extends CancellableEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final GameModule module;


    public ModuleEnableEvent(Game game, GameModule module) {
        this(false, game, module);
    }

    public ModuleEnableEvent(boolean isAsync, Game game, GameModule module) {
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
     * Gets the module that's about to be enabled.
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
