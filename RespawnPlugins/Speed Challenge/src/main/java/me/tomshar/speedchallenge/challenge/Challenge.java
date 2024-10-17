package me.tomshar.speedchallenge.challenge;

import me.tomshar.speedchallenge.util.Challenges;
import me.tomshar.speedchallenge.util.TimeSetting;

/**
 * Created by Tom on 10/03/14.
 */
public abstract class Challenge {

	private final Challenges challenge;
	private boolean netherEnabled = false;
	private boolean mobsSpawning = false;
	private TimeSetting time = TimeSetting.NORMAL;
	private boolean separateWorlds = true;
	private boolean friendlyFire = false;
	private boolean compassTracking = false;

	private boolean completed = false;

	public Challenge(Challenges challenge) {
		this.challenge = challenge;
	}

	public abstract boolean checkWinCondition();

	public String getName() {
		return challenge.getName();
	}

	public boolean isNetherEnabled() {
		return netherEnabled;
	}

	public void setNetherEnabled(boolean netherEnabled) {
		this.netherEnabled = netherEnabled;
	}

	public boolean isMobsSpawning() {
		return mobsSpawning;
	}

	public void setMobsSpawning(boolean mobsSpawning) {
		this.mobsSpawning = mobsSpawning;
	}

	public TimeSetting getTime() {
		return time;
	}

	public void setTime(TimeSetting time) {
		this.time = time;
	}

	public boolean isSeparateWorlds() {
		return separateWorlds;
	}

	public void setSeparateWorlds(boolean separateWorlds) {
		this.separateWorlds = separateWorlds;
	}

	public boolean isFriendlyFire() {
		return friendlyFire;
	}

	public void setFriendlyFire(boolean friendlyFire) {
		this.friendlyFire = friendlyFire;
	}

	public boolean isCompassTracking() {
		return compassTracking;
	}

	public void setCompassTracking(boolean compassTracking) {
		this.compassTracking = compassTracking;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void setCompleted(boolean completed) {
		this.completed = completed;
	}

}
