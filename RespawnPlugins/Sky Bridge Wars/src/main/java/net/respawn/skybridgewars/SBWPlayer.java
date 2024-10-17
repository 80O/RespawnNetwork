package net.respawn.skybridgewars;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.bukkit.entity.Player;


public class SBWPlayer extends GamePlayer<SkyBridgeWars> {

    public SBWPlayer(SkyBridgeWars game, Player player) {
        super(game, player);
    }

}
