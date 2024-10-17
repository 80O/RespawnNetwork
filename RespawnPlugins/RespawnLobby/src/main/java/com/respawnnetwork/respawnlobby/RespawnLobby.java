package com.respawnnetwork.respawnlobby;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.network.command.Command;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import com.respawnnetwork.respawnlib.plugin.Plugin;
import com.respawnnetwork.respawnlobby.runnables.*;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * The main respawn lobby plugin
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
@Getter
public class RespawnLobby extends Plugin {
    /** The server signs */
    private final List<GameServer> gameServers = new LinkedList<>();

    /** The signs holding the player scoreboards */
    private final List<ScoreboardSign> scoreboardSigns = new LinkedList<>();

    /** All status signs */
    private final List<Sign> statusSigns = new LinkedList<>();

    private final List<LobbyRunnable> runnables = new LinkedList<>();

    /** The navigation listener */
    private NavigationListener navigationListener;

    /** The welcome messages to send */
    private String[] welcomeMessages;

    /** The lobby spawn location */
    private Location spawnLocation;

    /** The player walk speed */
    private float walkSpeed;

    /** True if we should query player UUIDs */
    private boolean updateUUIDs = false;

    /** The server sign update task */
    private UpdateServerSigns signUpdateTask;

    /** The scoreboard sign update task */
    private UpdateScoreboardSigns scoreboardTask;

    /** The scoreboard sign update task */
    private UpdateServerPings serverPingTask;

    /** The scoreboard sign update task */
    private UpdateScoreboardData scoreboardData;

    /** The cooldowns update task */
    private CooldownsTask cooldownsTask;


    @Override
    public void onEnable() {
        super.onEnable();

        // Load default messages
        loadMessages("lobby.yml");

        // Register event listener
        registerListener(new LobbyEventListener(this));
        navigationListener = registerListener(new NavigationListener(this));

        // Start sign updates
        signUpdateTask = addRunnable(new UpdateServerSigns(this));
        signUpdateTask.runTaskTimer(this, 0, 5 * 20);

        scoreboardTask = addRunnable(new UpdateScoreboardSigns(this));
        scoreboardTask.runTaskTimer(this, 0, 10 * 60 * 20);

        serverPingTask = addRunnable(new UpdateServerPings(this));
        serverPingTask.runTaskTimerAsynchronously(this, 0, 3 * 20);

        scoreboardData = addRunnable(new UpdateScoreboardData(this));
        scoreboardData.runTaskTimerAsynchronously(this, 0, 10 * 60 * 20);

        cooldownsTask = addRunnable(new CooldownsTask(this));
        cooldownsTask.runTaskTimer(this, 0, 20);

        // Register commands
        CommandManager commandManager = getCommandManager();
        if (commandManager != null) {
            commandManager.registerCommands(this);
        }

        // Call internal reload method
        reload();
    }

    @Override
    public void onReload() {
        super.onReload();

        reload();
    }

    @Override
    public void onDisable() {
        super.onDisable();

        signUpdateTask.cancel();
        scoreboardTask.cancel();
        serverPingTask.cancel();
        scoreboardData.cancel();
        cooldownsTask.cancel();
    }

    @Command(name = "rlobby reload", permission = "respawn.lobby.rlobby reload", consoleCmd = true)
    public void onCommand(CommandSender sender, String[] args){
        reloadConfig();
        reload();
    }

    @Override
    public boolean usesInventoryMenuAPI() {
        return true;
    }

    /**
     * Returns the world of this lobby.
     *
     * @return The lobby world
     */
    public World getWorld() {
        return spawnLocation.getWorld();
    }

    /**
     * Loads a game server sign.
     *
     * @param serverName The server name
     * @param section The server's configuration section
     */
    public void loadServer(String serverName, ConfigurationSection section) {
        String address = section.getString("address");
        InetAddress inetAddress;

        try {
            inetAddress = InetAddress.getByName(address);

        } catch (UnknownHostException ex) {
            getPluginLog().log(Level.WARNING, "Could not get host for address: " + address, ex);
            return;
        }

        int port = section.getInt("port");
        String title = section.getString("title");
        String map = section.getString("map");

        // Create game server
        GameServer gameServer = new GameServer(inetAddress, port, serverName, title, map);
        gameServer.setPremiumOnly(section.getBoolean("premium", false));
        gameServer.setInvestorOnly(section.getBoolean("investor", false));
        gameServer.setVipOnly(section.getBoolean("vip", false));
        gameServer.setDisabled(section.getBoolean("disabled", false));

        // Get sign locations
        List<Location> locations = Location.parseList(getWorld(), section.getMapList("locations"));

        for (Location location : locations) {
            Sign sign = getSignAt(location);

            // Add sign
            if (sign != null) {
                gameServer.getSigns().add(sign);
            }
        }

        // Add game server
        getGameServers().add(gameServer);
    }

    private <R extends LobbyRunnable> R addRunnable(R runnable) {
        runnables.add(runnable);
        return runnable;
    }

    /**
     * Reloads the lobby plugin
     */
    private void reload() {
        // Get world
        String worldName = getConfig().getString("world");
        World world = getServer().getWorld(worldName);
        if (world == null) {
            getPluginLog().warning("No world with name " + worldName + " found, shutting down the lobby...");
            getServer().shutdown();
            return;
        }

        // Clear weather
        world.setStorm(false);
        world.setThundering(false);

        // Get spawn point
        ConfigurationSection spawnpoint = getConfig().getConfigurationSection("spawnPoint");

        if (spawnpoint != null) {
            spawnLocation = new Location(world, spawnpoint.getValues(false));

        } else {
            getPluginLog().info("No default spawn point set, will use default world spawn");
            spawnLocation = new Location(world.getSpawnLocation());
        }

        // Load server signs
        getGameServers().clear();

        ConfigurationSection serverSigns = getConfig().getConfigurationSection("signs.servers");
        if (serverSigns != null) {
            for(String key : serverSigns.getKeys(false)) {
                getPluginLog().info("Loading signs for game server with name: " + key);
                loadServer(key, serverSigns.getConfigurationSection(key));
            }

            getPluginLog().info("Loaded " + getGameServers().size() + " game server(s)!");

        } else {
            getPluginLog().warning("No server signs specified, players won't be able to join game servers!");
        }

        // Load status signs
        getStatusSigns().clear();

        for (Location location : Location.parseList(getWorld(), getConfig().getMapList("signs.status"))) {
            Sign sign = getSignAt(location);

            if (sign != null) {
                getStatusSigns().add(sign);
            }
        }

        getPluginLog().info("Loaded " + getStatusSigns().size() + " status signs!");

        // Load scoreboard signs
        getScoreboardSigns().clear();

        for (Map<?, ?> map : getConfig().getMapList("signs.scoreboard")) {
            ConfigurationSection scoreboardSignCfg = new MemoryConfiguration().createSection("tmp", map);

            ConfigurationSection signLocationCfg = scoreboardSignCfg.getConfigurationSection("signLocation");
            if (signLocationCfg == null) {
                getPluginLog().info("Scoreboard sign has no sign location!");
                continue;
            }

            ConfigurationSection skullLocationCfg = scoreboardSignCfg.getConfigurationSection("skullLocation");
            if (skullLocationCfg == null) {
                getPluginLog().info("Scoreboard sign has no skull location!");
                continue;
            }

            Location signLocation = new Location(getWorld(), signLocationCfg.getValues(false));
            Location skullLocation = new Location(getWorld(), skullLocationCfg.getValues(false));

            Sign sign = getSignAt(signLocation);

            BlockState blockState = skullLocation.getBlock().getState();
            if (!(blockState instanceof Skull)) {
                getPluginLog().info("Scoreboard skull is not a valid skull, at: " + skullLocation);
                continue;
            }

            Skull skull = ((Skull) blockState);

            // Finally add the scoreboard sign
            getScoreboardSigns().add(new ScoreboardSign(sign, skull));
        }

        // Check if this lobby should update player UUIDs
        this.updateUUIDs = getConfig().getBoolean("updateUUIDs");

        // Get player walk speed
        this.walkSpeed = 0.5f + (float) getConfig().getDouble("walkSpeed", 0.02);

        // Welcome messages
        Collection<String> messages = getConfig().getStringList("messages.welcome");
        if (messages == null) {
            // Just in case ...
            welcomeMessages = new String[] {"Welcome to the RespawnNetwork"};

        } else {
            welcomeMessages = new String[messages.size()];
            int i = 0;

            for (String message : messages) {
                welcomeMessages[i] = ChatColor.translateAlternateColorCodes('&', message);

                i++;
            }
        }

        // Reload all runnables
        for (LobbyRunnable runnable : runnables) {
            runnable.loadConfig(getConfig());
        }

        // Reload the compass navigation
        navigationListener.loadConfig(getConfig().getConfigurationSection("navigation"));
    }

    private Sign getSignAt(Location location) {
        return getSign(getWorld().getBlockAt(location));
    }

    Sign getSign(Block block) {
        if(block == null || !(block.getType() == Material.WALL_SIGN) && !(block.getType() == Material.SIGN_POST)) {
            return null;
        }

        // Add sign
        if(block.getState() instanceof Sign) {
            return (Sign) block.getState();
        }

        return null;
    }

}
