package net.respawn.havok.listeners;

import net.respawn.havok.HPlayer;
import net.respawn.havok.Havok;
import net.respawn.havok.Weapon;
import net.respawn.havok.util.GameState;
import net.respawn.havok.util.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.potion.PotionEffect;

/**
 * Created by Tom on 19/03/14.
 */
public class ConnectionListener implements Listener {

	private final Havok instance;

	public ConnectionListener(Havok instance) {
		this.instance = instance;
	}

	@EventHandler
	public void onPreLogin(AsyncPlayerPreLoginEvent e) {

        if (instance.game.getCurrentState() == GameState.END_GAME) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Game has just ended, please wait for the map to reset before joining.");
            return;
        }

        if(instance.game.getCurrentState() == GameState.IN_GAME && !instance.isWhitelisted(e.getUniqueId())) {

            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Please join when this game ends.");
            return;
        }
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();

		PlayerUtils.reset(player);

        for(PotionEffect potionEffect: player.getActivePotionEffects()) {

            player.removePotionEffect(potionEffect.getType());
        }

		player.setGameMode(GameMode.SURVIVAL);
        player.setLevel(0);

		player.teleport(instance.game.getRandomSpawn());

		// Get current players/max players/min players
		int currentPlayers = instance.getServer().getOnlinePlayers().length;
		int maxPlayers = Havok.instance.getConfig().getInt("maximumPlayers");
		int minPlayers = Havok.instance.getConfig().getInt("minimumPlayers");

		// Add tribute + join message
		if(instance.game.getCurrentState() == GameState.PRE_GAME && currentPlayers < maxPlayers) {

			String msg = Havok.instance.getConfig().getString("messages.join");
			msg = msg.replace("{player}", player.getName()).replace("{current}", String.valueOf(currentPlayers)).replace("{max}", String.valueOf(maxPlayers));

			e.setJoinMessage(ChatColor.translateAlternateColorCodes('&', msg));

            if(instance.getConfig().getStringList("greetings") == null) {return;}

            for(String message: instance.getConfig().getStringList("greetings")) {

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.replace("{kills-required}", instance.game.getRequiredKills() + "").replace("{map-name}", instance.game.getMapName()).replace("{change-interval}", instance.getConfig().getInt("changeInterval") + "")));
            }

            // Let players fly
            player.setAllowFlight(true);

            return;
		}

        if(instance.game.getCurrentState() == GameState.IN_GAME) {

            PlayerUtils.reset(player);
            player.setAllowFlight(false);
            player.setGameMode(GameMode.ADVENTURE);

            instance.game.refreshPotionEffects(player);
            player.teleport(instance.game.getRandomSpawn());
            player.setFallDistance(0f);

            Weapon wep = instance.game.getRandomWeapon(player.getUniqueId());

            HPlayer hPlayer = Havok.instance.game.getPlayers().get(e.getPlayer().getUniqueId());

            if(hPlayer == null) {

                instance.getLogger().severe("Failed to retrieve hPlayer entry for " + player.getName() + "!");
                return;
            }

            hPlayer.getPlayer().getInventory().clear();
            hPlayer.getPlayer().getInventory().setItem(0, wep.getItem());
            hPlayer.getPlayer().getInventory().setHeldItemSlot(0);
            hPlayer.setCurrentWeapon(wep);
            hPlayer.getPlayer().sendMessage(ChatColor.GREEN + "You have been given: " + wep.getName());
            hPlayer.getPlayer().setScoreboard(instance.scoreboard);
            hPlayer.getPlayer().setLevel(hPlayer.getKills());
            return;
        }

		if(currentPlayers == maxPlayers && instance.game.getCurrentState() == GameState.PRE_GAME) {
			instance.game.start();
		} else if(currentPlayers >= minPlayers) {
			instance.wcd.setWaitTime(10);
		} else if(currentPlayers == 1) {
			instance.wcd.setWaitTime(60);
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent e) {
		e.setQuitMessage("");
	}

	@EventHandler
	public void countDown(ServerListPingEvent event) {
		event.setMotd(instance.game.getCurrentState().getStatus());
	}

}
