package net.respawn.slicegames;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Date;

/**
 * Represent a player in Slice Games.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
@Getter
@Setter
public class SGPlayer extends GamePlayer<SliceGames> {
    /** The time this player died, might be null if he didn't die yet */
    private Date timeOfDeath;


    public SGPlayer(SliceGames game, Player player) {
        super(game, player);
    }

    public void reset() {
        clearInventory();

        // Get bukkit player
        Player player = getPlayer();
        if (player == null) {
            return;
        }

        // Reset health and stuff - whereas the health is half of the maximum
        player.setMaxHealth(40);
        player.setHealth(player.getMaxHealth() / 2);
        player.setLevel(0);
        player.setTotalExperience(0);
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setSaturation(20f);
        player.setExhaustion(0);
        player.setFallDistance(0);
    }

    public void giveCompass() {
        Player player = getPlayer();

        if (player != null) {
            player.getInventory().addItem(Item.getFor(Material.COMPASS,
                    ChatColor.BOLD.toString() + ChatColor.GREEN + "Nearest Player - Unknown"
            ));
        }
    }

}
