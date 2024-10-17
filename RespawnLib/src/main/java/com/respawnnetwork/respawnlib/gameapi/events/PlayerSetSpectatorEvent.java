package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets thrown whenever we set the spectator mode on a
 * game player.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class PlayerSetSpectatorEvent extends GamePlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    private final boolean spectator;


    public PlayerSetSpectatorEvent(GamePlayer gamePlayer, boolean spectator) {
        this(false, gamePlayer, spectator);
    }

    public PlayerSetSpectatorEvent(boolean isAsync, GamePlayer gamePlayer, boolean spectator) {
        super(isAsync, gamePlayer);
        this.spectator = spectator;
    }

    /**
     * Indicates if we want to enable or disable the spectator mode
     * for the given player.
     *
     * @return True if we will enable spectator, false if we want to disable
     */
    public boolean enableSpectator() {
        return spectator;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
