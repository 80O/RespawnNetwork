package me.tomshar.speedchallenge.util;

import java.io.File;

/**
 * Created by Tom on 10/03/14.
 */
public enum Challenges {
	WOOL_RACE("Wool Race", ""),
	MOB_HUNTER("Mob Hunter", ""),
	FOOD_CHALLENGE("Food Challenge", ""),
	DAYLIGHT_SENSOR("Daylight Sensor Race", ""),
	XP_RACE("XP Race", ""),
	ORE_FOR_POINTS("Ore for Points", ""),
	ENDER_DRAGON("Ender Dragon Race", "");

	private final String name;
	private final String fileName;

	private Challenges(String name, String fileName) {
		this.name = name;
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public File getLoadedFile() {
		return new File(fileName);
	}

	public String getName() {
		return name;
	}

}
