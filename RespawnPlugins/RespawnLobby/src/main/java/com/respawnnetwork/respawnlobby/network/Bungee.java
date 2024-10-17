package com.respawnnetwork.respawnlobby.network;

import com.respawnnetwork.respawnlib.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;


public final class Bungee {

    private Bungee() {
    }

    public static void teleport(final Plugin plugin, final Player player, final String serverName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                DataOutputStream dataOut = new DataOutputStream(byteOut);
                byte[] message;

                try {
                    dataOut.writeUTF("Connect");
                    dataOut.writeUTF(serverName);

                    message = byteOut.toByteArray();

                    byteOut.close();
                    dataOut.close();

                } catch (IOException io) {
                    plugin.getPluginLog().log(Level.SEVERE, "Could not prepare kick message", io);
                    return;
                }

                // Send message to player
                player.sendPluginMessage(plugin, com.respawnnetwork.respawnlib.network.bungee.Bungee.CHANNEL_NAME, message);
            }
        }.runTaskAsynchronously(plugin);
    }

}
