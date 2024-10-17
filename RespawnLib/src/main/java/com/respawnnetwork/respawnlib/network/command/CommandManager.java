package com.respawnnetwork.respawnlib.network.command;

import com.respawnnetwork.respawnlib.network.messages.Message;
import gnu.trove.map.hash.THashMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manager for the custom command API.
 * <p />
 * This class registers and manages all the commands using the RespawnLib CommandAPI.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class CommandManager implements CommandExecutor {
    /** The logger we use for errors */
    private final Logger log;

    /** The commands stored by their "name" */
    private final Map<String, Command> commands;

    /** The methods belonging to the commands */
    private final Map<Command, Method> methods;

    /** The objects having the methods */
    private final Map<Method, Object> instances;


    /**
     * Creates a new command manager instance.
     */
    public CommandManager(Logger log) {
        this.log = log;

        this.commands = new THashMap<>();
        this.methods = new THashMap<>();
        this.instances = new THashMap<>();
    }

    /**
     * Registers the commands of a certain object.
     *
     * @param object The object holding the annotated methods.
     * @return True on success, false if it failed (check the log files)
     */
    public boolean registerCommands(Object object) {
        boolean success = true;

        for (Method method : object.getClass().getMethods()) {
            try {
                Command cmd = method.getAnnotation(Command.class);
                if (cmd == null) {
                    continue;
                }

                Type returnType = method.getReturnType();
                if (returnType != Boolean.TYPE && returnType != Void.TYPE) {
                    throw new CommandException("Unsupported return type: " + returnType.toString());
                }

                Class<?>[] params = method.getParameterTypes();
                if (params.length != 2 || (params[0] != CommandSender.class) || !(params[1].isArray())) {
                    throw new CommandException("Method " + method.getName() + " does not match the required parameters!");
                }

                commands.put(cmd.name().toLowerCase(), cmd);

                for (String s : cmd.aliases()) {
                    commands.put(s.toLowerCase(), cmd);
                }

                methods.put(cmd, method);
                instances.put(method, object);

            } catch (CommandException e) {
                log.log(Level.WARNING, "Error registering command" , e);
                success = false;
            }
        }

        return success;
    }

    @Override
    public boolean onCommand(CommandSender cs, org.bukkit.command.Command command, String label, String[] args) {
        String str;
        Command cmd;

        for (int argsLeft = args.length; argsLeft >= 0; argsLeft--) {
            // Build command string
            str = label + ((argsLeft > 0) ? " " + StringUtils.join(Arrays.copyOfRange(args, 0, argsLeft)) : "");

            // Try to get the command - if it can't be found, continue searching
            cmd = getCommand(str);

            if (cmd == null) {
                continue;
            }

            // We have one, execute it!
            try {
                // Check for console / player
                if (!(cs instanceof Player) && (cs instanceof ConsoleCommandSender && !cmd.consoleCmd())) {
                    throw new CommandException("The command cannot be executed from console.");
                }

                // Check permissions
                if (cmd.opOnly() && !cs.isOp() || !hasPermission(cs, cmd.permission())) {
                    throw new CommandException("You are not allowed to use that command!");
                }

                // Check if we want to display the help
                boolean help = args.length > 0 && "?".equalsIgnoreCase(args[args.length - 1]);

                if (!help) {
                    String[] cmdArgs = Arrays.copyOfRange(args, argsLeft, args.length);

                    if (cmdArgs.length < cmd.minArgs() || (cmd.maxArgs() >= 0 && cmdArgs.length < cmd.maxArgs())) {
                        help = true;

                    } else {
                        Method method = methods.get(cmd);

                        if (method == null) {
                            throw new CommandException("Something went wrong: Method not found. Please call an admin!");
                        }

                        if (method.getReturnType() == Boolean.TYPE) {
                            help = !((Boolean) method.invoke(getInstance(method), cs, cmdArgs));

                        } else {
                            method.invoke(getInstance(method), cs, cmdArgs);
                        }
                    }
                }

                if (help) {
                    Message.CUSTOM.send(cs, "&cHelp: &3%s", cmd.name());

                    String desc = cmd.description();
                    Message.CUSTOM.send(cs, "&6Description&f: %s", desc.isEmpty() ? "&7(no description available)" : desc);

                    String usage = cmd.usage();
                    Message.CUSTOM.send(cs, "&6Usage: &f%s", usage.isEmpty() ? "/" + cmd.name() : usage);

                    if (cmd.aliases().length > 0) {
                        Message.CUSTOM.send(cs, "&6Aliases: &f/%s", StringUtils.join(cmd.aliases(), ", /"));
                    }
                }

                return true;

            } catch(CommandException ce) {
                if (!cmd.hide()) {
                    // Send exception message
                    Message.DANGER.send(cs, ce.getMessage());
                    return true;

                } else {
                    // Return default not found message
                    return false;
                }

            } catch(InvocationTargetException | IllegalAccessException | RuntimeException ex) {
                // Log detailed error
                log.log(Level.SEVERE, "Error executing command", ex);
                Message.DANGER.send(cs, "&6Oh no, an error! Please contact an admin!");

                return true;
            }
        }

        // Send error message
        if (args.length > 0) {
            Message.DANGER.send(cs, "Command not found: &6%s &7(/%s %s)", args[args.length - 1], label, StringUtils.join(args));

        } else {
            Message.DANGER.send(cs, "Command not found: &6%s", label);
        }

        return true;
    }

    /**
     * Indicates whether or not a command sender has the specified permission.
     *
     * @param sender The sender to check
     * @param perm The permission to check
     * @return True if he does, false if not
     */
    public boolean hasPermission(CommandSender sender, String perm) {
        if ((!(sender instanceof Player)) || (perm == null) || (perm.isEmpty())) {
            return true;
        }

        Player player = (Player) sender;
        return player.isOp() || player.hasPermission(perm);
    }

    /**
     * Gets a command by its label.
     *
     * @param label The command label
     * @return The found command, or null
     */
    public Command getCommand(String label) {
        return commands.get(label);
    }

    /**
     * Returns a collection of all registered commands.
     *
     * @return All the registered commands in a collection
     */
    public Collection<Command> getCommands() {
        return commands.values();
    }

    /**
     * Sends a detailed help screen to the sender.
     *
     * @param name The name to display in the title
     * @param cs The command sender that requested the help
     */
    public void showHelp(String name, CommandSender cs) {
        Message.CUSTOM.send(cs, "&3" + (name == null ? "" : name) + " Help");
        Message.CUSTOM.send(cs, "&2Add a '?' after each command for a detailed help (like /xyz ?)" );

        Collection<Command> commands = getCommands();

        if(commands != null && !commands.isEmpty()) {
            for (Command cmd : commands) {
                if (!hasPermission(cs, cmd.permission()) || !cmd.showInHelp()) {
                    continue;
                }

                String desc = cmd.description().isEmpty() ? "&7(no description available)" : cmd.description() ;
                Message.CUSTOM.send( cs, "&6/%s: &f%s", cmd.name(), desc );
            }
        } else {
            Message.CUSTOM.send(cs, "&7(No commands available)");
        }
    }


    private Object getInstance(Method m) {
        return instances.get(m);
    }


    private static class CommandException extends Exception {

        private CommandException(String message) {
            super(message);
        }

    }


}
