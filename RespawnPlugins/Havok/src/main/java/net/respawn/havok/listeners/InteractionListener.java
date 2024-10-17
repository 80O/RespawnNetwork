package net.respawn.havok.listeners;

import net.respawn.havok.HPlayer;
import net.respawn.havok.Havok;
import net.respawn.havok.util.GameState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * Created by Tom on 19/03/14.
 */
public class InteractionListener implements Listener {

	private final Havok instance;

	public InteractionListener(Havok instance) {
		this.instance = instance;
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
        e.setCancelled(true);

		Action action = e.getAction();
		Player player = e.getPlayer();
		Material material = e.getMaterial();

		HPlayer hPlayer = instance.game.getPlayers().get(player.getUniqueId());

		if(instance.game.getCurrentState() == GameState.IN_GAME) {

            if(hPlayer == null) {
                e.setCancelled(true);
                return;
            }

			if(hPlayer.getCurrentWeapon().isOnCooldown()) {
				long elapsed = System.currentTimeMillis() - hPlayer.getLastWeaponUseTime();
				if(elapsed >= (hPlayer.getCurrentWeapon().getCooldown() * 1000)) {
					hPlayer.getCurrentWeapon().setOnCooldown(false);
				} else {
					return;
				}
			}

			if(material != null) {
				// Blaze rod
				if(material == Material.BLAZE_ROD) {
					/**
					 * Minimized this and moved into the createLightningHit on the HGame.
					 */

                    for(int i = 0; i < 5; i++) {

                        instance.game.createLightningHit(player);
                    }

					hPlayer.setLastWeaponUseTime(System.currentTimeMillis());
					hPlayer.getCurrentWeapon().setOnCooldown(true);
				}
				// Wither Skulls - 1 second cooldown
				else if(material == Material.COAL) {
					for (int i = 0; i < 3; i++) {
						shoot(player.launchProjectile(WitherSkull.class), player, 1.0F);
						hPlayer.setLastWeaponUseTime(System.currentTimeMillis());
						hPlayer.getCurrentWeapon().setOnCooldown(true);
					}
				}
				// Fire Ball - 2 second cooldown
				else if (material == Material.FIREBALL)
				{
					shoot(player.launchProjectile(Fireball.class), player, 0.0F);
					hPlayer.setLastWeaponUseTime(System.currentTimeMillis());
					hPlayer.getCurrentWeapon().setOnCooldown(true);
				}
				// Snowball - 0.5 second cooldown
				else if (material == Material.SNOW_BALL)
				{
					player.launchProjectile(Snowball.class);
                    ItemStack stack = player.getInventory().getItemInHand();
                    stack.setAmount(16);
					player.getInventory().setItemInHand(stack);
					hPlayer.setLastWeaponUseTime(System.currentTimeMillis());
					hPlayer.getCurrentWeapon().setOnCooldown(true);
				}
				// Diamond - 0.1 second cooldown
				else if(material == Material.DIAMOND && (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) {
					arrow(player.launchProjectile(Arrow.class), player);
					hPlayer.setLastWeaponUseTime(System.currentTimeMillis());
					hPlayer.getCurrentWeapon().setOnCooldown(true);
				}
			}

		}

	}

	@EventHandler
	public void onProjectileHit(ProjectileHitEvent e) {
		Entity entity = e.getEntity();

		if (entity instanceof Snowball) {

			Snowball snowball = (Snowball) entity;
			ProjectileSource shooter = snowball.getShooter();
			if (shooter instanceof Player) {
				Player player = (Player) shooter;
				Location sb = snowball.getLocation();
				List<Entity> entities = snowball.getNearbyEntities(2.0D, 2.0D, 2.0D);

                for (Entity ent : entities) {
					if (ent instanceof Player) {
						((Player)ent).damage(3.0D, player);

						EntityDamageByEntityEvent ev = new EntityDamageByEntityEvent(snowball, ent, EntityDamageEvent.DamageCause.PROJECTILE, 3.0D);
						instance.getServer().getPluginManager().callEvent(ev);
					}
				}

                player.getWorld().createExplosion(sb.getX(), sb.getY(), sb.getZ(), 0.0F, false, false);
			}
		} else if (entity instanceof Arrow) {
			entity.remove();
		} else if (entity instanceof Fireball) {

            Fireball fireball = (Fireball) entity;
            ProjectileSource shooter = fireball.getShooter();
            if (shooter instanceof Player) {
                Player player = (Player) shooter;
                Location sb = fireball.getLocation();
                List<Entity> entities = fireball.getNearbyEntities(2.0D, 2.0D, 2.0D);

                for(Entity ent : entities) {
                    if(ent instanceof Player) {
                        ((Player) ent).damage(20D, player);

                        EntityDamageByEntityEvent ev = new EntityDamageByEntityEvent(fireball, ent, EntityDamageEvent.DamageCause.PROJECTILE, 20.0D);
                        instance.getServer().getPluginManager().callEvent(ev);
                    }
                }

                player.getWorld().createExplosion(sb.getX(), sb.getY(), sb.getZ(), 0.0F, false, false);
            }
        }
	}

	private void shoot(Fireball projectile, Player player, float yield) {
		// Really shouldnt use depracted methods.
		projectile.setShooter(player);
		projectile.setIsIncendiary(false);
		projectile.setYield(yield);
	}

	private void arrow(Arrow arrow, Player player) {
		// Really shouldnt use depracted methods.
		arrow.setShooter(player);
		Vector vec = player.getLocation().getDirection();
		float speed = 0.18F;

		arrow.setVelocity(new Vector(vec.getX() * speed * 3.D, vec.getY() * speed * 3.5D, vec.getZ() * speed * 3.0D));
	}
}
