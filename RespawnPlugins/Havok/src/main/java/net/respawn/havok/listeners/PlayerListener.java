package net.respawn.havok.listeners;

import net.respawn.havok.HPlayer;
import net.respawn.havok.Havok;
import net.respawn.havok.runnables.RespawnDelayTask;
import net.respawn.havok.util.GameState;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Tom on 19/03/14.
 */
public class PlayerListener implements Listener {

	private final Havok instance;

	public PlayerListener(Havok instance) {
		this.instance = instance;
	}

    @EventHandler
    public void playerDeath(final PlayerDeathEvent e) {
        e.getDrops().clear();
        e.setDroppedExp(0);

        if(e.getEntity() == null || !(e.getEntity() instanceof Player) || instance.game.getCurrentState() != GameState.IN_GAME) {return;}

        if(instance.game.getCurrentState() == GameState.IN_GAME) {

            Player killedPlayer = (Player) e.getEntity();
            HPlayer killedHavokPlayer = instance.game.getPlayers().get(killedPlayer.getUniqueId());

            if(!(killedHavokPlayer == null)) {
                killedHavokPlayer.incrementDeaths();
            }

            EntityDamageEvent damageEvent = killedPlayer.getLastDamageCause();

            boolean reasonSet = false;

            if(damageEvent instanceof EntityDamageByEntityEvent) {

                EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) damageEvent;

                if(entityDamageByEntityEvent.getDamager() == null && !reasonSet) {
                    e.setDeathMessage(ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " was killed by the wind");
                    reasonSet = true;
                }

                if(!(entityDamageByEntityEvent.getDamager() instanceof Player) && !reasonSet) {
                    e.setDeathMessage(ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " was killed by a monster");
                }

                if(entityDamageByEntityEvent.getDamager() instanceof Player && !reasonSet) {
                    Player killer = (Player) entityDamageByEntityEvent.getDamager();
                    HPlayer killerHavokPlayer = instance.game.getPlayers().get(killer.getUniqueId());

                    if(killedPlayer.getUniqueId().equals(killer.getUniqueId())) {
                        e.setDeathMessage(ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " died of their own " + "[" + instance.game.getPlayers().get(killedPlayer.getUniqueId()).getCurrentWeapon().getItem().getItemMeta().getDisplayName() + "]");
                        reasonSet = true;
                    } else {

                        if(!(killerHavokPlayer == null)) {

                            killerHavokPlayer.incrementKills();

                            // Update scoreboard data
                            for (HPlayer hPlayer : instance.game.getPlayers().values()) {
                                if(hPlayer == null || hPlayer.getPlayer() == null) {continue;}
                                instance.objective.getScore(hPlayer.getPlayer().getName()).setScore(hPlayer.getKills());
                                hPlayer.getPlayer().setLevel(hPlayer.getKills());
                            }

                            // Display scoreboard
                            for (Player onlinePlayer : instance.getServer().getOnlinePlayers()) {
                                onlinePlayer.setScoreboard(instance.scoreboard);
                            }

                            if(killerHavokPlayer.getKills() >= instance.game.getRequiredKills()) {

                                e.setDeathMessage(ChatColor.BLUE + killer.getDisplayName() + ChatColor.RESET + " killed " + ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " with their " + "[" + instance.game.getPlayers().get(killerHavokPlayer.getUniqueId()).getCurrentWeapon().getItem().getItemMeta().getDisplayName() + "]");
                                instance.game.end(killer);
                                return;
                            }
                        }

                        e.setDeathMessage(ChatColor.BLUE + killer.getDisplayName() + ChatColor.RESET + " killed " + ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " with their " + "[" + instance.game.getPlayers().get(killerHavokPlayer.getUniqueId()).getCurrentWeapon().getItem().getItemMeta().getDisplayName() + "]");
                        reasonSet = true;
                    }
                }
            }

            if((damageEvent.getCause() == EntityDamageEvent.DamageCause.FIRE || damageEvent.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) && !reasonSet) {
                e.setDeathMessage(ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " burned to death");
                reasonSet = true;
            }

            if(damageEvent.getCause() == EntityDamageEvent.DamageCause.DROWNING && !reasonSet) {
                e.setDeathMessage(ChatColor.GOLD + killedPlayer.getDisplayName() + ChatColor.RESET + " drowned");
                reasonSet = true;
            }

            // Auto respawn.
            new BukkitRunnable() {
                public void run() {
                    try {
                        Object nmsPlayer = e.getEntity().getClass().getMethod("getHandle").invoke(e.getEntity());
                        Object con = nmsPlayer.getClass().getDeclaredField("playerConnection").get(nmsPlayer);

                        Class<?> EntityPlayer = Class.forName(nmsPlayer.getClass().getPackage().getName() + ".EntityPlayer");

                        Field minecraftServer = con.getClass().getDeclaredField("minecraftServer");
                        minecraftServer.setAccessible(true);
                        Object mcserver = minecraftServer.get(con);

                        Object playerlist = mcserver.getClass().getDeclaredMethod("getPlayerList").invoke(mcserver);
                        Method moveToWorld = playerlist.getClass().getMethod("moveToWorld", EntityPlayer, int.class, boolean.class);
                        moveToWorld.invoke(playerlist, nmsPlayer, 0, false);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }.runTaskLater(instance, 1);
        }
    }

	@EventHandler
	public void playerRespawn(PlayerRespawnEvent e) {
        HPlayer hPlayer = instance.game.getPlayers().get(e.getPlayer().getUniqueId());

		e.setRespawnLocation(instance.game.getRandomSpawn());

		if (instance.game.getCurrentState() == GameState.IN_GAME) {
            /**
             * Compensate for bukkit weirdness
             */
             new RespawnDelayTask(instance.game, hPlayer).runTaskLater(instance, 1);
		}
	}

    @EventHandler
    public void playerGainExp(PlayerExpChangeEvent e) {
        e.setAmount(0);
    }

	@EventHandler
	public void playerDamageByPlayer(EntityDamageByEntityEvent e) {
		if (instance.game.getCurrentState() != GameState.IN_GAME) {
            e.setCancelled(true);
        }

		if (e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			Player damager = (Player) e.getDamager();
			Player damaged = (Player) e.getEntity();

			// Wood Axe - Poison for 30 seconds.
			if (damager.getItemInHand().getType() == Material.WOOD_AXE) {
				damaged.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 30 * 20, 1));
			}
			// Diamond - Stun for 3 seconds
			else if (damager.getItemInHand().getType() == Material.DIAMOND) {
				damaged.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 100, 128));
                damaged.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 100, 6));
                damaged.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                damaged.setVelocity(damaged.getVelocity().setX(0).setY(0).setZ(0));
			}
		}
	}

	@EventHandler
	public void playerDamage(EntityDamageEvent e) {
		if (instance.game.getCurrentState() != GameState.IN_GAME || e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }

		/**
		 * This is where the check for lightning hits gets calculated.
		 */
		if(e.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING) && e.getEntity() instanceof Player){
			Player damaged = (Player) e.getEntity();
			HPlayer hplayer = instance.game.getLightningAttacker(damaged.getLocation());
			if (hplayer != null){
				damaged.damage(5.0, hplayer.getPlayer());
			}
			e.setCancelled(true);
		}
	}

    @EventHandler
    public void playerDropItem(PlayerDropItemEvent e) {

        e.setCancelled(true);
    }

	@EventHandler
	public void onFoodlevel(FoodLevelChangeEvent e) {
		e.setCancelled(true);
	}

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if(e.getPlayer().getLocation().getY() < 0) {

            e.getPlayer().teleport(Havok.instance.game.getRandomSpawn());
            return;
        }
    }

}
