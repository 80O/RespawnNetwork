package im.xpd.parkour.SpecialCondition;

public enum SpecialConditionOperator {
	LESS_THAN, EQUAL_TO, MORE_THAN;
	
	public static SpecialConditionOperator fromString(String str) {
		switch (str.trim()) {
			case "<": return LESS_THAN;
			case "=": return EQUAL_TO;
			case ">": return MORE_THAN;
			default: return null;
		}
	}
}
