package com.respawnnetwork.respawnlib.network.command;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a callable command.
 *
 * @author spaceemotion
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {
    /** The command name (like "xyz set key value") */
    public String name();

    /** The command description, can be left empty */
    public String description() default "";

    /** A detailed usage description, can be left empty to create an auto-generated usage message */
    public String usage() default "";

    /** The needed permission. If empty, this won't check anything */
    public String permission() default "";

    /** Other aliases for this command */
    public String[] aliases() default {};

    /** The minimum number of arguments */
    public int minArgs() default 0;

    /** The maximum number of arguments */
    public int maxArgs() default -1;

    /** True if the command can also be executed from the console */
    public boolean consoleCmd() default false;

    /** True if the command can only be executed by operators */
    public boolean opOnly() default false;

    /** True if the command should be listed in help screens */
    public boolean showInHelp() default true;

    /** True if the command should be hidden, this will return the default "command not found" message by bukkit */
    public boolean hide() default false;
}
