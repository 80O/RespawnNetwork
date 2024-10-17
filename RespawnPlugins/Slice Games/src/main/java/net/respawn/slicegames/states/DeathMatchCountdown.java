package net.respawn.slicegames.states;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import com.respawnnetwork.respawnlib.math.MersenneTwisterFast;
import com.respawnnetwork.respawnlib.network.messages.Message;
import net.respawn.slicegames.SGPlayer;
import net.respawn.slicegames.SliceGames;
import net.respawn.slicegames.modules.SliceGamesModule;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Represents the death match countdown state in a Slice Games game.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class DeathMatchCountdown extends CountdownState<SliceGames> implements InGameState {
    public static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    private int deathArenaSpread;


    public DeathMatchCountdown(SliceGames game) {
        super(game);
    }

    @Override
    protected boolean onLoad() {
        deathArenaSpread = getConfig().getInt("arenaSpread");

        return super.onLoad();
    }

    @Override
    protected void onEnter() {
        Location center = null;
        SliceGamesModule sliceGamesModule = getGame().getModule(SliceGamesModule.class);

        if (sliceGamesModule != null) {
            center = sliceGamesModule.getDeathMatchCenter();
        }

        if (center == null) {
            getLogger().warning("No death match center location set, will skip death match!");
            getGame().nextState();
            return;
        }

        // Immediately go to the next state when we already have a winner
        if (getGame().getMap().getWinner() != null) {
            getGame().nextState();
            return;
        }

        // Teleport all still playing players to a random location around the death match center
        for(SGPlayer player : getGame().getRealPlayers()) {
            // Multiply times -1 or 1 for the offset
            player.teleportTo(center.add(
                    (RANDOM.nextBoolean() ? 1d : -1d) * RANDOM.nextInt(deathArenaSpread),
                    0,
                    (RANDOM.nextBoolean() ? 1d : -1d) * RANDOM.nextInt(deathArenaSpread)));
        }

        // Teleport all spectators to the death center
        for (SGPlayer spectator : getGame().getSpectators()) {
            spectator.teleportTo(center);
        }

        super.onEnter();

        Message.INFO.sendKey("sg.deathMatchStarted");
    }

    @EventHandler
    public void damage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        // Cancel all damage in the countdown
        if (!getGame().isSpectator(((Player) event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @Override
    public String getName() {
        return "death-match-countdown";
    }

    @Override
    public String getDisplayName() {
        return "DeathMatch Countdown";
    }


}
