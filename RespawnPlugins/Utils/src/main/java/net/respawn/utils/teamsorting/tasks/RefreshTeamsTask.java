package net.respawn.utils.teamsorting.tasks;

import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.respawn.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.List;

public class RefreshTeamsTask extends BukkitRunnable {

    private Utils plugin;

    private TMap<String, String> teamCache = null;

    public RefreshTeamsTask(Utils plugin) {

        this.plugin = plugin;

        teamCache = new THashMap<>();
    }

    int counter = 0;

    @Override
    public void run() {

        if(counter < plugin.getConfig().getInt("teams.refresh-time")) {

            counter++;
            return;
        }

        if(counter >= plugin.getConfig().getInt("teams.refresh-time")) {

            if(plugin.getServer().getOnlinePlayers().length == 0) {

                teamCache.clear();
            }

            for(Player player: plugin.getServer().getOnlinePlayers()) {
                if(teamCache.keySet().contains(String.valueOf(player.getUniqueId())) && teamCache.get(String.valueOf(player.getUniqueId())).equals(plugin.permission.getPrimaryGroup(player))) {continue;}

                if(teamCache.keySet().contains(String.valueOf(player.getUniqueId()))) {
                    Team oldTeam = plugin.getScoreboard().getTeam(teamCache.get(String.valueOf(player.getUniqueId())));

                    List<OfflinePlayer> playersToRemove = new ArrayList<OfflinePlayer>();

                    for(OfflinePlayer teamPlayer : oldTeam.getPlayers()) {
                        if(String.valueOf(teamPlayer.getUniqueId()).equalsIgnoreCase(String.valueOf(player.getUniqueId()))) {
                            playersToRemove.add(teamPlayer);
                        }
                    }

                    for(OfflinePlayer offlinePlayer: playersToRemove) {
                        oldTeam.removePlayer(offlinePlayer);
                    }
                }

                if(!plugin.getConfig().getBoolean("teams.auto-sort-players") || plugin.permission == null) {
                    return;
                }

                Team pTeam = plugin.getScoreboard().getTeam(plugin.permission.getPrimaryGroup(player));

                if(pTeam == null) {
                    pTeam = plugin.getScoreboard().registerNewTeam(plugin.permission.getPrimaryGroup(player));
                }

                if(!(plugin.getConfig().get("teams.groupColors." + plugin.permission.getPrimaryGroup(player)) == null)) {

                    pTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("teams.groupColors." + plugin.permission.getPrimaryGroup(player))));
                }

                pTeam.addPlayer(player);

                teamCache.put(String.valueOf(player.getUniqueId()), plugin.permission.getPrimaryGroup(player));

                player.setScoreboard(plugin.getScoreboard());
            }

            counter = 0;
        }
    }
}
