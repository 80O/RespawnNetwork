package net.respawn.utils;

import com.respawnnetwork.respawnlib.network.command.Command;
import com.respawnnetwork.respawnlib.network.command.CommandManager;
import com.respawnnetwork.respawnlib.network.messages.Message;
import com.respawnnetwork.respawnlib.plugin.Plugin;
import net.milkbowl.vault.permission.Permission;
import net.respawn.utils.teamsorting.listeners.PlayerTeamSortListener;
import net.respawn.utils.teamsorting.tasks.RefreshTeamsTask;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;

public class Utils extends Plugin {

    public static Permission permission = null;

    private Scoreboard scoreboard = null;

    private RefreshTeamsTask teamsTask = null;

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onEnable() {
        super.onEnable();

        loadMessages("utils.yml");

        CommandManager commandManager = getCommandManager();

        if(commandManager != null) {
            commandManager.registerCommands(this);
        }

        if(getServer().getPluginManager().getPlugin("Vault") != null && getServer().getPluginManager().getPlugin("Vault").isEnabled()) {
            setupPermissions();

            if(permission == null) {

                getPluginLog().warning(Message.CUSTOM.parseKey("utils.permissionsNotFound"));
            } else {

                getPluginLog().info(Message.CUSTOM.parseKey("utils.vaultHooked"));
            }
        } else {
            getPluginLog().warning(Message.CUSTOM.parseKey("utils.vaultNotFound"));
        }

        scoreboard = getServer().getScoreboardManager().getNewScoreboard();

        // Team sorting listener
        registerListener(new PlayerTeamSortListener(this));

        teamsTask = new RefreshTeamsTask(this);
        teamsTask.runTaskLater(this, 1200);
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);

        if (permissionProvider != null) {
            permission = permissionProvider.getProvider();
        }
        return (permission != null);
    }

    @Command(name="utils", description="Main plugin command, displays help", permission="respawn.admin", consoleCmd=true)
    public void utilsCommand(CommandSender sender, String[] args) {

        CommandManager commandManager = getCommandManager();

        if(commandManager != null) {
            commandManager.showHelp(getName(), sender);
        }
    }

    @Command(name="utils reload", description="Reloads the plugin", permission="respawn.reload", consoleCmd=true)
    public void utilsReloadCommand(CommandSender sender, String[] args) {

        reloadConfig();

        if(sender instanceof Player) {

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix").replace("{name}", getDescription().getName()) + Message.CUSTOM.parseKey("utils.reload")));
            return;
        }

        sender.sendMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', getConfig().getString("prefix").replace("{name}", getDescription().getName()) + Message.CUSTOM.parseKey("utils.reload"))));
        return;
    }

    public boolean usesInventoryMenuAPI() {
        return true;
    }

    /**
     * Returns the scoreboard for the plugin
     * @return the scoreboard for the plugin
     */
    public Scoreboard getScoreboard() {
        return scoreboard;
    }
    
    @Override
    public void onDisable() {
        super.onDisable();

        scoreboard = null;
        teamsTask.cancel();

        permission = null;
    }
}
