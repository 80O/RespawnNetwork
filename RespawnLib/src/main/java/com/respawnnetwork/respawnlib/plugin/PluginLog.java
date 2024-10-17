package com.respawnnetwork.respawnlib.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.*;

/**
 * Represents a custom plugin logger.
 * <p />
 * Handles the debug output for plugin.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class PluginLog extends Logger {
    /** The default log folder name */
    public static final String DEFAULT_LOG_FOLDER = "logs";

    /** The private file handler */
    private FileHandler handler;


    /**
     * Creates a new custom plugin logger.
     *
     * @param plugin The plugin to use
     */
    public PluginLog(Plugin plugin) {
        this(plugin, "plugin", "");
    }

    /**
     * Creates a new custom plugin logger.
     *
     * @param plugin The plugin to use
     * @param name The name of the logger
     * @param folder The name of the folder, starting at "(data folder)/logs"
     */
    public PluginLog(Plugin plugin, String name, String folder) {
        super(name, null);

        DebugFormatter formatter = new DebugFormatter(plugin.getLogger());

        try {
            File logFolder = new File(plugin.getDataFolder(), DEFAULT_LOG_FOLDER + File.separatorChar + folder);

            if (!logFolder.exists()) {
                plugin.getLogger().info("Creating log folder '" + logFolder.getAbsolutePath() + "'...");

                if (!logFolder.mkdirs()) {
                    plugin.getLogger().severe("Error creating log folder. Please check permissions!");
                }
            }

            handler = new FileHandler(new File(logFolder, name + ".log").getAbsolutePath(), true);
            addHandler(handler);
            setLevel(Level.ALL);
            handler.setFormatter(formatter);

        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error creating logger '" + name + '\'', ex);
        }
    }

    /**
     * Closes the log file.
     */
    public void close() {
        if (this.handler == null) {
            return;
        }

        this.handler.close();
    }


    private static class DebugFormatter extends Formatter {
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        private final Logger parentLogger;


        private DebugFormatter(Logger parentLogger) {
            this.parentLogger = parentLogger;
        }

        @Override
        public String format(LogRecord rec) {
            Throwable exception = rec.getThrown();

            // First show it in the console
            parentLogger.log(rec.getLevel(), rec.getMessage(), exception);

            // Then store it in the custom logger
            StringBuilder out = new StringBuilder();
            out.append(DATE_FORMAT.format(rec.getMillis()));

            out.append("[").append(rec.getLevel().getName().toUpperCase()).append("] ");
            out.append(rec.getMessage()).append('\r').append('\n');

            if (exception != null) {
                StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));

                out.append(writer);
            }

            return out.toString();
        }

    }

}
