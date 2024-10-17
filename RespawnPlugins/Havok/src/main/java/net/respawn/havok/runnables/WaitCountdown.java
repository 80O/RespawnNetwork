package net.respawn.havok.runnables;

import net.respawn.havok.HPlayer;
import net.respawn.havok.Havok;
import net.respawn.havok.util.GameState;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

/**
 * Created by Tom on 15/03/14.
 */
public class WaitCountdown extends BukkitRunnable {

	private final Havok instance;
	private int minimum;
	private int waitTime;
	private int seconds = 0;

    private boolean starting = false;

	public WaitCountdown(Havok instance) {
		this.instance = instance;
		this.minimum = instance.getConfig().getInt("minimumPlayers");
		this.waitTime = instance.getConfig().getInt("waitBeforeStarting");
        instance.objective.setDisplayName("Waiting for §a" + (minimum - instance.getServer().getOnlinePlayers().length) + "§f players.");
	}

	@Override
	public void run() {

        if(instance.game.getCurrentState() == GameState.IN_GAME || instance.game.getCurrentState() == GameState.END_GAME) {return;}

		if(instance.getServer().getOnlinePlayers().length > 0 && instance.game.getCurrentState() == GameState.PRE_GAME) {

            instance.objective.getScore("Players:").setScore(instance.getServer().getOnlinePlayers().length);

            if(!starting && instance.getServer().getOnlinePlayers().length < minimum) {
                starting = false;
                instance.objective.setDisplayName("Waiting for §a" + (minimum - instance.getServer().getOnlinePlayers().length) + "§f players.");

                for(Player p : instance.getServer().getOnlinePlayers()) {
                    p.setScoreboard(instance.scoreboard);
                }

                return;
            }

            if(!starting && instance.getServer().getOnlinePlayers().length >= minimum) {

                starting = true;
                seconds = waitTime;

                for(Player p : instance.getServer().getOnlinePlayers()) {
                    p.setScoreboard(instance.scoreboard);
                }

                return;
            }

            if(starting && seconds > 0 && instance.getServer().getOnlinePlayers().length >= minimum) {

                instance.objective.setDisplayName("Starting in: §a" + seconds);
                seconds--;

                for(Player p : instance.getServer().getOnlinePlayers()) {
                    p.setScoreboard(instance.scoreboard);
                }

                return;
            }

            if(starting && seconds <= 0 && instance.getServer().getOnlinePlayers().length >= minimum) {

				instance.game.start();

				for(HPlayer player : instance.game.getPlayers().values()) {
					player.getPlayer().setAllowFlight(false);
				}

				return;
			}
		}
	}

	public void setWaitTime(int waitTime) {
		this.waitTime = waitTime;
		this.seconds = 0;
	}

	public void setMinimum(int minimum) { this.minimum = minimum; }
}
