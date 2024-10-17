package net.respawn.slicegames.states;

import com.respawnnetwork.respawnlib.gameapi.states.DefaultInGameState;
import net.respawn.slicegames.SGPlayer;
import net.respawn.slicegames.SliceGames;
import net.respawn.slicegames.modules.SliceGamesModule;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Timestamp;

/**
 * Represents the in-game state for Slice Games.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class InSGState extends DefaultInGameState<SliceGames> {
    private BukkitTask refillTask;
    private BukkitTask endGameTask;


    public InSGState(SliceGames game) {
        super(game);
    }

    @Override
    public void onEnter() {
        // Set map start time
        getGame().getMap().setStartTime(new Timestamp(System.currentTimeMillis()));

        // Apply grace period resistance potion
        for(SGPlayer sgPlayer : getGame().getRealPlayers()) {
            Player player = sgPlayer.getPlayer();

            if (player == null) {
                continue;
            }

            player.addPotionEffect(PotionEffectType.DAMAGE_RESISTANCE.createEffect(getConfig().getInt("gracePeriod", 10) * 20, 2));
        }

        // Start refill timer
        refillTask = new BukkitRunnable() {
            @Override
            public void run() {
                SliceGamesModule module = getGame().getModule(SliceGamesModule.class);

                if (module != null) {
                    module.refillInventories(true);

                } else {
                    getLogger().info("No SliceGames module found, cannot refill inventories!");
                }
            }
        }.runTaskLater(getGame().getPlugin(), getConfig().getInt("refillInventoriesAfter", 10) * 20);

        // Start main countdown
        endGameTask = new BukkitRunnable() {
            @Override
            public void run() {
                // Prevent from double cancellation
                endGameTask = null;
                cancel();

                // Go to next state
                getGame().nextState();
            }
        }.runTaskLater(getGame().getPlugin(), getConfig().getInt("endAfter", 300) * 20);

        // Let the games begin!
        super.onEnter();
    }

    @Override
    protected void onLeave() {
        super.onLeave();

        if (refillTask != null) {
            refillTask.cancel();
        }

        if (endGameTask != null) {
            endGameTask.cancel();
        }
    }

}
