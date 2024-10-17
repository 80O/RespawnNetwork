package net.respawn.havok.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.event.world.StructureGrowEvent;

/**
 * Created by Tom on 19/03/14.
 */
public class WorldListener implements Listener
{

	@EventHandler
	public void onStructureGrow(StructureGrowEvent event)
	{
		event.setCancelled(true);
	}

	@EventHandler
	public void onPortalCreate(PortalCreateEvent event)
	{
		event.setCancelled(true);
	}

    @EventHandler
    public void weatherChange(WeatherChangeEvent e) {
        e.setCancelled(true);
    }
}
