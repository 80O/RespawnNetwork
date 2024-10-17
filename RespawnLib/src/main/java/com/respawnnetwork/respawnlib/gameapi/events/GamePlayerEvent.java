package com.respawnnetwork.respawnlib.gameapi.events;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import lombok.Getter;
import org.bukkit.event.Event;

/**
 * Represents an abstract game player event.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
abstract class GamePlayerEvent extends Event {
    @Getter
    private final GamePlayer gamePlayer;


    protected GamePlayerEvent(GamePlayer gamePlayer) {
        this(false, gamePlayer);
    }

    protected GamePlayerEvent(boolean isAsync, GamePlayer gamePlayer) {
        super(isAsync);

        this.gamePlayer = gamePlayer;
    }

}
