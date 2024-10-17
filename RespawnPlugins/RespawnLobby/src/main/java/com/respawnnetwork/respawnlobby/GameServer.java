package com.respawnnetwork.respawnlobby;

import com.respawnnetwork.respawnlib.network.signs.SignBuilder;
import com.respawnnetwork.respawnlib.plugin.Plugin;
import com.respawnnetwork.respawnlobby.network.Bungee;
import com.respawnnetwork.respawnlobby.network.RemoteServer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Represents a game server that holds its data and the signs.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
@Getter
@Setter
public class GameServer extends RemoteServer {
    public static final int DEFAULT_TIMEOUT = 7000;
    public static final String DIVIDER = ChatColor.DARK_RED.toString() + ChatColor.BOLD + "█████████";

    private final Collection<Sign> signs;
    private final String bungeeServerName;
    private final String title;
    private final String map;
    private String motd;
    private boolean premiumOnly;
    private boolean vipOnly;
    private boolean investorOnly;
    private boolean disabled;
    private boolean offline;
    private int online;
    private int max;


    /**
     * Creates a new server sign.
     *
     * @param address The server address
     * @param port The server port
     * @param title The sign title
     * @param map The game map
     */
    public GameServer(InetAddress address, int port, String serverName, String title, String map) {
        super(address, port);

        this.signs = new LinkedList<>();

        this.bungeeServerName = serverName;
        this.title = title;
        this.map = map;
    }

    /**
     * Teleports the specified player to the game server.
     *
     * @param player The player
     */
    public void teleport(final Plugin plugin, final Player player) {
        Bungee.teleport(plugin, player, getBungeeServerName());
    }

    /**
     * Updates the sign contents by pulling the latest server information.
     * <p />
     * Displays a restart message if the server MOTD equals "ended" or we couldn't connect to the server.
     */
    public void update() {
        // Build server sign
        SignBuilder signBuilder = new SignBuilder();
        signBuilder.add(getSigns());
        signBuilder.line(0, getMap());

        if (isVipOnly()) {
            signBuilder.line(1, "&l" + ChatColor.LIGHT_PURPLE + "-VIP-");

        } else if (isInvestorOnly()) {
            signBuilder.line(1, "&l" + ChatColor.AQUA + "-Investor-");

        } else if (isPremiumOnly()) {
            signBuilder.line(1, "&l" + ChatColor.GREEN + "-Premium-");
        }

        if(this.isOffline()) {
            signBuilder.line(2, DIVIDER);
            signBuilder.line(3, ChatColor.DARK_RED + "- Offline -");

        } else {
            signBuilder.provide("online", getOnline()).provide("max", getMax())
                    .line(2, "{online}/{max}")
                    .line(3, getMotd());
        }

        signBuilder.apply();
    }

}
