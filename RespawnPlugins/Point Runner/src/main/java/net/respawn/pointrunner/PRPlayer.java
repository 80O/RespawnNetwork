package net.respawn.pointrunner;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.bukkit.entity.Player;


public class PRPlayer extends GamePlayer<PointRunner>{

    public PRPlayer(PointRunner game, Player player) {
        super(game, player);
    }

    /**
     * Resets the level to zero and displays map message.
     */
    public void resetPlayer() {
        Player bukkitPlayer = getPlayer();

        if (bukkitPlayer != null) {
            bukkitPlayer.setLevel(0);
        }
    }

}
