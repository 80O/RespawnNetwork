package net.respawn.havok;

import com.respawnnetwork.respawnlib.network.database.Database;
import net.respawn.havok.commands.MainCMD;
import net.respawn.havok.listeners.*;
import net.respawn.havok.runnables.WaitCountdown;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Tom on 19/03/14.
 */
public class Havok extends JavaPlugin {

	public static Havok instance;
	public Database database = null;

    public HGame game;

	public WaitCountdown wcd;

	public ScoreboardManager manager;
	public Scoreboard scoreboard;
	public Objective objective;

    private List<UUID> uuidWhiteList = null;

    private HeartBeat heartBeat;

	@Override
	public void onEnable() {

        saveDefaultConfig();

        instance = this;

        if(getConfig().getBoolean("giveTokens")) {
            if(!getConfig().getBoolean("mysql.enabled")) {
                getLogger().severe("The database is not enabled! No tokens will be given while it is disabled!");
            } else {
                database = new Database(getLogger(), getConfig().getString("mysql.host"), getConfig().getString("mysql.port"), getConfig().getString("mysql.database"), getConfig().getString("mysql.user"), getConfig().getString("mysql.password"));
                database.open();
                database.close();
                getLogger().info("Connected to database!");
            }
        }

		Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		// Scoreboard stuff
		manager = getServer().getScoreboardManager();
		scoreboard = manager.getNewScoreboard();

        Havok.instance.objective = Havok.instance.scoreboard.registerNewObjective("countdown", "dummy");
        Havok.instance.objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        uuidWhiteList = new ArrayList<>();

		this.game = new HGame(getConfig().getString("map"));

        heartBeat = new HeartBeat(game);
        heartBeat.runTaskTimer(Havok.instance, 0, 20);

        getCommand("game").setExecutor(new MainCMD(this));

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new BlockListener(this), this);
		pm.registerEvents(new ConnectionListener(this), this);
		pm.registerEvents(new PlayerListener(this), this);
		pm.registerEvents(new InventoryListener(this), this);
		pm.registerEvents(new InteractionListener(this), this);
		pm.registerEvents(new EntityListener(this), this);

		pm.registerEvents(new WorldListener(), this);

        wcd = new WaitCountdown(this);
        wcd.runTaskTimer(this, 0, 20);
	}

    public void onDisable() {

        objective = null;
        scoreboard = null;
        manager = null;

        if(getServer().getScheduler().isCurrentlyRunning(heartBeat.getTaskId())) {
            getServer().getScheduler().cancelTask(heartBeat.getTaskId());
        }

        if(getServer().getScheduler().isCurrentlyRunning(wcd.getTaskId())) {
            wcd.cancel();
        }
        wcd = null;

        uuidWhiteList.clear();
        uuidWhiteList = null;

        game = null;

        instance = null;
    }

    /**
     * whitelists the provided UUID
     * @param UUID the UUID to whitelist
     */
    public void whitelistUUID(UUID UUID) {

        if(uuidWhiteList.contains(UUID)) {return;}

        uuidWhiteList.add(UUID);
    }

    /**
     * unwhitelists the provided UUID
     * @param UUID the UUID to unwhitelist
     */
    public void unWhitelistUUID(UUID UUID) {

        if(!uuidWhiteList.contains(UUID)) {return;}

        uuidWhiteList.remove(UUID);
    }

    /**
     * Clears the UUID list
     */
    public void clearUUIDs() {

        uuidWhiteList.clear();
    }

    /**
     * Returns if the UUID is whitelisted
     * @param UUID the UUID to check for
     * @return if the UUID is whitelisted
     */
    public boolean isWhitelisted(UUID UUID) {

        return uuidWhiteList.contains(UUID);
    }

    /**
     * Returns the game's heartbeat
     * @return the game's heartbeat
     */
    public HeartBeat getHeartBeat() {

        return heartBeat;
    }
}
