package net.respawn.slicegames.runnables;

import com.respawnnetwork.respawnlib.bukkit.Item;
import lombok.RequiredArgsConstructor;
import net.respawn.slicegames.SGPlayer;
import net.respawn.slicegames.SliceGames;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Represents a task that updates all spectator compasses.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
@RequiredArgsConstructor
public class UpdateCompasses extends BukkitRunnable {
    private final SliceGames game;


    @Override
    public void run() {
        for(SGPlayer sgPlayer : game.getPlayers()) {
            Player player = sgPlayer.getPlayer();
            if (player == null) {
                continue;
            }

            ItemStack compass = player.getItemInHand();

            if (compass == null || !Material.COMPASS.equals(compass.getType())) {
                continue;
            }

            // Find nearest player
            SGPlayer closest = null;
            Location pLoc = player.getLocation();

            for(SGPlayer other : game.getRealPlayers()) {
                if(sgPlayer.equals(other)) {
                    continue;
                }

                if(closest != null) {
                    Player otherBukkitPlayer = other.getPlayer();

                    if (otherBukkitPlayer != null) {
                        Location otherLocation = otherBukkitPlayer.getLocation();

                        Player closestPlayer = closest.getPlayer();
                        if (closestPlayer != null && pLoc.distance(otherLocation) < pLoc.distance(closestPlayer.getLocation())) {
                            closest = other;
                        }
                    }

                } else {
                    closest = other;
                }
            }

            if(closest != null) {
                Player closestPlayer = closest.getPlayer();
                if (closestPlayer == null) {
                    continue;
                }

                Item.setDisplayName(compass, "§a§lNearest Player - " + ((int) pLoc.distance(closestPlayer.getLocation())) + "M");
                player.setCompassTarget(closest.getPlayer().getLocation());
            }
        }
    }

}
