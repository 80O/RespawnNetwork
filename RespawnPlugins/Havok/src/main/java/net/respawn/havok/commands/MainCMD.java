package net.respawn.havok.commands;

import net.respawn.havok.Havok;
import net.respawn.havok.util.GameState;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCMD implements CommandExecutor {

    private Havok plugin = null;

    public MainCMD(Havok plugin) {

        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(args.length == 0) {

            sender.sendMessage("Usage: /game help");
            return true;
        }

        if(args.length > 0) {

            if(args[0].equalsIgnoreCase("help")) {

                usage(sender);
                return true;
            }

            if(args[0].equalsIgnoreCase("reload")) {

                if(!sender.hasPermission("respawn.reload")) {

                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                        return true;
                    }

                    sender.sendMessage("You do not have permission to do that!");
                    return true;
                }

                plugin.reloadConfig();

                if(sender instanceof Player) {

                    sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    return true;
                }

                sender.sendMessage("Config reloaded!");
                return true;
            }

            if(args[0].equalsIgnoreCase("start")) {

                if(!sender.hasPermission("respawn.havok.start")) {

                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                        return true;
                    }

                    sender.sendMessage("You do not have permission to do that!");
                    return true;
                }

                if(plugin.game.getCurrentState() != GameState.PRE_GAME) {

                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.RED + "Unable to force start game!");
                        return true;
                    }

                    sender.sendMessage("Unable to force start game!");
                    return true;
                }

                plugin.game.start();

                if(sender instanceof Player) {

                    sender.sendMessage(ChatColor.GREEN + "Game force started!");
                    return true;
                }

                sender.sendMessage("Game force started!!");
                return true;
            }

            if(args[0].equalsIgnoreCase("stop")) {

                if(!sender.hasPermission("respawn.havok.stop")) {

                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                        return true;
                    }

                    sender.sendMessage("You do not have permission to do that!");
                    return true;
                }

                if(plugin.game.getCurrentState() != GameState.IN_GAME) {

                    if(sender instanceof Player) {

                        sender.sendMessage(ChatColor.RED + "Unable to force stop game!");
                        return true;
                    }

                    sender.sendMessage("Unable to force stop game!");
                    return true;
                }

                plugin.game.end(null);

                if(sender instanceof Player) {

                    sender.sendMessage(ChatColor.GREEN + "Game force stopped!");
                    return true;
                }

                sender.sendMessage("Game force stopped!!");
                return true;
            }

            usage(sender);
            return true;
        }

        return true;
    }

    private void usage(CommandSender sender) {

        sender.sendMessage("Subcommands: reload - reloads the plugin configuration \n start - forces the game to start \n stop - forces the game to stop");
    }
}
