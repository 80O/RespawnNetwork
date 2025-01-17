package uk.co.jacekk.bukkit.infiniteplots;

import net.minecraft.server.v1_7_R2.WorldData;
import net.minecraft.server.v1_7_R2.WorldServer;
import net.minecraft.server.v1_7_R2.WorldType;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.world.WorldInitEvent;

import uk.co.jacekk.bukkit.baseplugin.event.BaseListener;
import uk.co.jacekk.bukkit.baseplugin.util.ReflectionUtils;
import uk.co.jacekk.bukkit.infiniteplots.generation.PlotsGenerator;

public class WorldInitListener extends BaseListener<InfinitePlots> {
	
	public WorldInitListener(InfinitePlots plugin){
		super(plugin);
	}
	
	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
	public void onWorldInit(WorldInitEvent event){
		World world = event.getWorld();
		
		if (world.getGenerator() instanceof PlotsGenerator){
			this.plugin.getPlotManager().loadPlotsFor(world);
			
			WorldServer worldServer = ((CraftWorld) world).getHandle();
			
			try{
				ReflectionUtils.setFieldValue(WorldData.class, "type", worldServer.worldData, WorldType.FLAT);
				
				plugin.log.info("Changed the world type of '" + world.getName() + "' to flat (this makes the void blue down to y = 0).");
			}catch (Exception e){
				plugin.log.info("Could not change the world type of '" + world.getName() + "'.");
				e.printStackTrace();
			}
		}
	}
	
}
