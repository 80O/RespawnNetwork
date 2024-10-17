package net.respawn.havok;

import gnu.trove.map.hash.THashMap;
import net.respawn.havok.util.GameState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public class HeartBeat extends BukkitRunnable {

    private HGame game = null;

    private Map<UUID, Integer> playerWeaponChangeMap = null;

    public HeartBeat(HGame game) {

        this.game = game;
        playerWeaponChangeMap = new THashMap<UUID, Integer>();
    }

    /**
     * resets the weapon change count for the given UUID
     * @param UUID the UUID to reset the count of
     */
    public void resetUUIDCount(UUID UUID) {

        if(!playerWeaponChangeMap.keySet().contains(UUID)) {return;}

        playerWeaponChangeMap.put(UUID, 0);
    }

    @Override
    public void run() {
        if(game.getCurrentState() == GameState.END_GAME) {return;}

        if(Havok.instance.getServer().getOnlinePlayers().length == 0 && game.getCurrentState() == GameState.IN_GAME) {game.end(null); return;}
        if(Havok.instance.getServer().getOnlinePlayers().length == 1 && game.getCurrentState() == GameState.IN_GAME) {
            Player[] players = Havok.instance.getServer().getOnlinePlayers();
            game.end(players[0]);
        }

        for(HPlayer hPlayer: game.getPlayers().values()) {
            if(hPlayer.getPlayer() == null || hPlayer.getCurrentWeapon() == null) {continue;}

            if(!playerWeaponChangeMap.keySet().contains(hPlayer.getUniqueId())) {
                playerWeaponChangeMap.put(hPlayer.getUniqueId(), 0);
            }

            int count = playerWeaponChangeMap.get(hPlayer.getUniqueId());

            if(count >= Havok.instance.getConfig().getInt("changeInterval")) {

                Weapon wep = game.getRandomWeapon(hPlayer.getUniqueId());
                hPlayer.getPlayer().getInventory().clear();
                hPlayer.getPlayer().getInventory().setItem(0, wep.getItem());
                hPlayer.getPlayer().getInventory().setHeldItemSlot(0);
                hPlayer.setCurrentWeapon(wep);
                playerWeaponChangeMap.put(hPlayer.getUniqueId(), 0);
                hPlayer.getPlayer().sendMessage(ChatColor.GREEN + "You have been given: " + wep.getName());
                continue;
            }

            playerWeaponChangeMap.put(hPlayer.getUniqueId(), ++count);
        }
    }
}
