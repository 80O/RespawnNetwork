package com.respawnnetwork.respawnlib.network.bungee;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BungeeChannelListener implements PluginMessageListener {
    private final Logger logger;


    public BungeeChannelListener(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        // Just for safety reasons
        if (!channel.equals(Bungee.CHANNEL_NAME)) {
            return;
        }

        DataInputStream in = null;

        try {
            // Create stream
            in = new DataInputStream(new ByteArrayInputStream(message));

            int i = in.read();


        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not read message stream", e);

        } finally {
            // Close input stream
            try {
                if (in != null) {
                    in.close();
                }

            } catch (IOException e) {
                logger.log(Level.WARNING, "Could not close message stream", e);
            }
        }
    }

}
