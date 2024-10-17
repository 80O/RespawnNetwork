package im.xpd.parkour;

import lombok.Data;

@Data
public class ParkourBlock {
	private final int id;
	private final byte data;
	private final ParkourBlockType type;
}
