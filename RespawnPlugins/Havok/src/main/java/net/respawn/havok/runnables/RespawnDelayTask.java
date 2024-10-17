package net.respawn.havok.runnables;


import net.respawn.havok.HGame;
import net.respawn.havok.HPlayer;
import net.respawn.havok.Havok;
import net.respawn.havok.Weapon;
import org.bukkit.ChatColor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnDelayTask extends BukkitRunnable {

    HGame game = null;
    HPlayer hPlayer = null;

    public RespawnDelayTask(HGame game, HPlayer hPlayer) {

        this.game = game;
        this.hPlayer = hPlayer;
    }

    public void run() {

        Weapon wep = game.getRandomWeapon(hPlayer.getUniqueId());

        hPlayer.setCurrentWeapon(wep);
        hPlayer.getPlayer().getInventory().clear();
        hPlayer.getPlayer().getInventory().setItem(0, wep.getItem());
        hPlayer.getPlayer().getInventory().setHeldItemSlot(0);
        hPlayer.getPlayer().sendMessage(ChatColor.GREEN + "You have been given: " + wep.getName());

        for (PotionEffect effect : game.getPotionEffects()) {
            hPlayer.getPlayer().addPotionEffect(effect);
        }

        hPlayer.getPlayer().setLevel(hPlayer.getKills());

        Havok.instance.getHeartBeat().resetUUIDCount(hPlayer.getUniqueId());
    }
}
