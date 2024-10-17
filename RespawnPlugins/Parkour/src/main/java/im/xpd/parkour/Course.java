package im.xpd.parkour;

import com.respawnnetwork.respawnlib.network.signs.SignBuilder;
import im.xpd.parkour.SpecialCondition.SpecialCondition;
import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;

import java.util.List;

@Data
public class Course {
	private final String worldName;
	private final Location start;
	private final Location lobbySign;
	private final String name;
	private final String author;
	private final int checkpointYTrigger;
	private final List<ParkourBlock> blocks;
	private final int totalPoints;
	private final List<SpecialCondition> specialConditions;
	private final List<CoursePotionEffect> potionEffects;
	private final String mode;
	private final List<String> modeBlocks;
    private final String permission;


	public void updateCourseSign() {
		if (!(lobbySign.getBlock().getState() instanceof Sign)) {
            return;
        }

        // Use sign builder and update the contents
        new SignBuilder((Sign) lobbySign.getBlock().getState())
                .provide("name", name).provide("author", author)
                .lines("{name}", "by {author}", "", ChatColor.ITALIC + "Click to play")
                .apply();
	}
	
	public ParkourBlock getBlock(int id, byte data) {
		for (ParkourBlock block : blocks) {
			if (block.getId() == id && block.getData() == data) {
				return block;
			}
		}
		return null;
	}

}
