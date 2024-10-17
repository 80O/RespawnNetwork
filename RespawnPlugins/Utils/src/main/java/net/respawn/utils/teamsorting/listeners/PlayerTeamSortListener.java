package net.respawn.utils.teamsorting.listeners;

import net.respawn.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;

public class PlayerTeamSortListener implements Listener {

    private Utils plugin = null;

    public PlayerTeamSortListener(Utils plugin) {

        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent e) {

        if(!plugin.getConfig().getBoolean("teams.auto-sort-players") || plugin.permission == null) {return;}

        Team pTeam = plugin.getScoreboard().getTeam(plugin.permission.getPrimaryGroup(e.getPlayer()));

        if(pTeam == null) {
            pTeam = plugin.getScoreboard().registerNewTeam(plugin.permission.getPrimaryGroup(e.getPlayer()));
        }

        if(!(plugin.getConfig().get("teams.groupColors." + plugin.permission.getPrimaryGroup(e.getPlayer())) == null)) {

            pTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("teams.groupColors." + plugin.permission.getPrimaryGroup(e.getPlayer()))));
        }

        pTeam.addPlayer(e.getPlayer());

        e.getPlayer().setScoreboard(plugin.getScoreboard());
    }
}
