package im.xpd.parkour;

import lombok.Data;
import org.bukkit.block.Block;

@Data
public class LogBlock {
	private final Block block;
	private final ParkourBlockType type;
}
