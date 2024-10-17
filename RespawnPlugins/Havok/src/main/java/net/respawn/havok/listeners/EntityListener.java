package net.respawn.havok.listeners;

import net.respawn.havok.Havok;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Created by Tom on 19/03/14.
 */
public class EntityListener implements Listener {

	private final Havok instance;

	public EntityListener(Havok instance) {
		this.instance = instance;
	}

	@EventHandler
	public void onExplode(EntityExplodeEvent e) {
		e.setCancelled(true);
	}

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if(e.getEntity() instanceof Player) {return;}
        e.setCancelled(true);
    }
}
