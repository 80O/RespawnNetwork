package im.xpd.parkour.SpecialCondition;

import im.xpd.parkour.Course;
import im.xpd.parkour.PPlayer;
import im.xpd.parkour.Parkour;

import org.bukkit.entity.Player;

public class SpecialCondition {
	private SpecialConditionType type;
	private SpecialConditionOperator operator;
	private double value;
	private SpecialConditionAction action;


	public SpecialCondition(SpecialConditionType type, SpecialConditionOperator operator, double value, SpecialConditionAction action) {
		this.type = type;
		this.operator = operator;
		this.value = value;
		this.action = action;
	}
	
	public SpecialConditionType getType() {
		return type;
	}
	
	public boolean test(double value) {
		switch (operator) {
			case LESS_THAN: return value < this.value;
			case EQUAL_TO: return value == this.value;
			case MORE_THAN: return value > this.value;
		}

		return false;
	}
	
	public SpecialConditionAction getAction() {
		return action;
	}
	
	public void takeAction(Parkour parkour, Player player) {
		PPlayer pPlayer = parkour.getPPlayer(player.getName());
		Course course = parkour.getCourseByWorldName(player.getWorld().getName());
		if (pPlayer != null) {
			if (action == SpecialConditionAction.TP_TO_CHECKPOINT) {
				player.teleport(pPlayer.getCheckPoint(course).getLocation());
			}
		}
	}
	
	public static SpecialCondition fromString(String str) {
		String[] args = str.split(" ");
		if (args.length == 4) {
			SpecialConditionType type = SpecialConditionType.valueOf(args[0]);
			SpecialConditionOperator operator = SpecialConditionOperator.fromString(args[1]);
			double value = 0;
			try {
				value = Double.parseDouble(args[2]);
			} catch (Exception e) {
				return null;
			}
			SpecialConditionAction action = SpecialConditionAction.valueOf(args[3]);
			if (type == null || operator == null || action == null) {
				return null;
			}
			return new SpecialCondition(type, operator, value, action);
		}
		return null;
	}
}
