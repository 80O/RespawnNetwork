package com.respawnnetwork.respawnlib.gameapi.modules.team.events;

import com.respawnnetwork.respawnlib.bukkit.events.CancellableEvent;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import org.bukkit.event.HandlerList;

/**
 * Represents an event that gets called whenever a player joins a team.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class PlayerJoinsTeamEvent extends CancellableEvent {
    private static final HandlerList handlers = new HandlerList();

    private final Game game;
    private final GamePlayer player;
    private final Team team;


    public PlayerJoinsTeamEvent(Game game, GamePlayer player, Team team) {
        this(false, game, player, team);
    }

    public PlayerJoinsTeamEvent(boolean isAsync, Game game, GamePlayer player, Team team) {
        super(isAsync);
        this.game = game;
        this.player = player;
        this.team = team;
    }

    public Game getGame() {
        return game;
    }

    public GamePlayer getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
