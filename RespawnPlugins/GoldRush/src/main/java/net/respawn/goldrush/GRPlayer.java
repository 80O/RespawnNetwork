package net.respawn.goldrush;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;


public class GRPlayer extends GamePlayer<GoldRush> {

    public GRPlayer(GoldRush game, Player player) {
        super(game, player);
    }

    public void reset() {
        heal();

        Player player = getPlayer();

        if (player != null) {
            player.setTotalExperience(0);
            player.setLevel(0);
        }
    }

    public void addSpeed() {
        Player player = getPlayer();

        if (player != null) {
            player.addPotionEffect(PotionEffectType.SPEED.createEffect(Integer.MAX_VALUE, 0), true);
        }
    }

}
