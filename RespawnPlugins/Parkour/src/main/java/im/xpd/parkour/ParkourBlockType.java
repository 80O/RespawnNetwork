package im.xpd.parkour;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ParkourBlockType {
	TOKEN(-2,0),
	LOBBY(-1, 0),
	CHECKPOINT(0, 0),
	ONE_POINT(1, 1),
	TWO_POINTS(2, 2),
	THREE_POINTS(3, 3);

    @Getter
	private int id;

    @Getter
    private int points;


	public static ParkourBlockType getById(int id) {
		for (ParkourBlockType pbt : values()) {
			if (pbt.getId() == id) {
				return pbt;
			}
		}
		return null;
	}

}
