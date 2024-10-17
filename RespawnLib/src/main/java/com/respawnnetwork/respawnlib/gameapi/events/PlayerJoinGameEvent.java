package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets thrown whenever a player joins a game.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class PlayerJoinGameEvent extends GamePlayerEvent {
    private static final HandlerList handlers = new HandlerList();


    public PlayerJoinGameEvent(GamePlayer gamePlayer) {
        super(gamePlayer);
    }

    public PlayerJoinGameEvent(boolean isAsync, GamePlayer gamePlayer) {
        super(isAsync, gamePlayer);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
