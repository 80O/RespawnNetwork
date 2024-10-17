package im.xpd.parkour;

import lombok.Data;
import org.bukkit.Location;

@Data
public class Lobby {
	private final Location spawn;
	private final int backToSpawnY;
}
