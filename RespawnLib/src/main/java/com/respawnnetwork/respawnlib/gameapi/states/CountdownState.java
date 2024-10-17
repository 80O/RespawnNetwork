package com.respawnnetwork.respawnlib.gameapi.states;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.GameState;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Represents a state that counts down until it switches to the next state.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class CountdownState<G extends Game> extends GameState<G> implements Listener {
    @Getter
    private boolean freezePlayers;

    @Getter
    private int seconds;

    /** The countdown task */
    private BukkitTask taskTimer;


    public CountdownState(G game) {
        super(game);
    }

    @Override
    protected boolean onLoad() {
        freezePlayers = getConfig().getBoolean("freezePlayers", false);
        seconds = getConfig().getInt("seconds", 10);

        return true;
    }

    @Override
    protected void onEnter() {
        // Start timer
        taskTimer = new CountdownTask(seconds).runTaskTimer(getGame().getPlugin(), 20, 20);
    }

    @Override
    protected void onLeave() {
        if (taskTimer != null) {
            taskTimer.cancel();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.isCancelled() || !freezePlayers) {
            return;
        }

        // Don't do anything if we haven't moved in distance (maybe just rotated the head, or jumped)
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ()) {
            return;
        }

        // Only cancel if the player is in the game
        // We cancel it by setting the new location to the old block location but keeping the yaw and pitch changes
        GamePlayer player = getGame().getPlayer(event.getPlayer());
        if (player != null && !player.isSpectator()) {
            event.setTo(new Location(
                    from.getWorld(),
                    from.getBlockX() + 0.5d, from.getBlockY() + 0.125d, from.getBlockZ() + 0.5d,
                    to.getYaw(), to.getPitch()
            ));
        }
    }

    protected void onFinishedCountdown() {
        getGame().nextState();
        getGame().playSound(getFinishSound(), 2, 1);
    }

    /**
     * Gets the sound for each countdown.
     *
     * @return The sound playing on each countdown
     */
    protected Sound getCountdownSound() {
        return Sound.NOTE_PLING;
    }

    /**
     * Gets the finish sound, might return null if we don't want to have one.
     *
     * @return The finish sound
     */
    protected Sound getFinishSound() {
        return Sound.FIREWORK_LAUNCH;
    }

    /**
     * Returns the "offset" from this countdown.
     * <p />
     * If you specify an offset of 3, this countdown will count to n-3, where n being
     * the total countdown seconds (Does that make sense? D:).
     *
     * @return The countdown offset
     */
    protected int getOffset() {
        return 0;
    }

    @Override
    public String getDisplayName() {
        return "Countdown";
    }

    @Override
    public String getName() {
        return "countdown";
    }


    private class CountdownTask extends BukkitRunnable {
        private int secondsLeft;


        private CountdownTask(int seconds) {
            this.secondsLeft = seconds;
        }

        @Override
        public void run() {
            if (secondsLeft > 0 && ((secondsLeft % 10 == 0) || secondsLeft <= 5)) {
                getGame().createMessage().provide("seconds", secondsLeft).sendKey("game." + getName() + ".message");

                // Play the "ding" sound as countdown signal
                getGame().playSound(getCountdownSound(), 2, 2 - ((float) secondsLeft * 0.075f));

            } else if (secondsLeft <= 0) {
                finish();
                return;
            }

            secondsLeft--;

            // Check if we're under the limit and finish early
            if (secondsLeft <= getOffset()) {
                finish();
            }
        }

        private void finish() {
            cancel();
            onFinishedCountdown();
        }

    }

}
