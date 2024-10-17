package im.xpd.parkour;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.block.Block;

@Data
public class Checkpoint {
	private final Block block;
	private final Location location;
}
