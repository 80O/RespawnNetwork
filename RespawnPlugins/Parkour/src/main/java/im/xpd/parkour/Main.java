package im.xpd.parkour;

import com.respawnnetwork.respawnlib.network.command.Command;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.plugin.Plugin;
import gnu.trove.map.hash.THashMap;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;

import java.sql.SQLException;
import java.util.Map;

public class Main extends Plugin {
	public Parkour parkour;
	public Inventory in;
    public Database database;
	public Map<String,String> inicons = new THashMap<>();


	public void onEnable() {
        super.onEnable();

        getPluginLog().info("Running Parkour version " + this.getDescription().getVersion());

		final EventListener el = registerListener(new EventListener(this));

        CommandManager commandManager = getCommandManager();
        if (commandManager != null) {
            commandManager.registerCommands(this);
        }

        this.database = new Database(this);

        try {
            if ((!database.connectLite()) && (!database.connectSQL(getConfig().getString("database.host"), getConfig().getString("database.port"), getConfig().getString("database.user"), getConfig().getString("database.pass"),getConfig().getString("database.name")))) {
                getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to connect to database.");
                getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Disabling myself, good night.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }catch(Exception ex){
            this.getLogger().info("Error loading databases!");
        }

        parkour = new Parkour(this);
		
		for (Player player : getServer().getOnlinePlayers()) {
			parkour.loadPPlayer(player.getName());
		}

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                el.blockCheckLoop();
            }
        }, 0, 5);

        // Register custom chat stuff
        if (getPluginDependency() != null && getPluginDependency().isInstalled("Vault")) {
            registerListener(new VaultChatListener(this));
        }
    }

    @Command(name = "checkpoint")
    public void onCheckpointCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Message.DANGER.sendKey("parkour.error.playerExpected");
            return;
        }

        Player player = (Player) sender;
        Course course = parkour.getCourseByWorldName(player.getWorld().getName());

        if (course != null) {
            player.teleport(parkour.getPPlayer(sender.getName()).getCheckPoint(course).getLocation());

        } else {
            sender.sendMessage(ChatColor.GOLD + "You must be playing a course to use /checkpoint.");
        }
    }

    @Command(name = "return")
    public void onReturnCmd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            Message.DANGER.sendKey("parkour.error.playerExpected");
            return;
        }

        Player player = (Player) sender;
        Course course = parkour.getCourseByWorldName(player.getWorld().getName());

        //teleports player and clears potions
        player.teleport(parkour.getLobby().getSpawn());

        for(PotionEffect pe : player.getActivePotionEffects()){
            player.removePotionEffect(pe.getType());
        }

        PPlayer pPlayer = parkour.getPPlayer(player.getName());
        if (pPlayer != null) {
            pPlayer.updateExpBar(parkour, null);
        }
    }
	
	public void onDisable() {
		for (Player player : getServer().getOnlinePlayers()) {
			parkour.unloadPPlayer(player.getName());
		}
		
		try {
			database.getLiteConnection().close();
			database.getSqlConnection().close();

		} catch (SQLException e) {
			getServer().getConsoleSender().sendMessage(ChatColor.RED + "[Parkour] Failed to close database connection.");
			e.printStackTrace();
		}
	}
}
