package me.tomshar.speedchallenge;

import org.bukkit.entity.Player;

/**
 * Created by Tom on 10/03/14.
 */
public class Participate {

	private final String name;
	private Team team;

	public Participate(String name) {
		this.name = name;
	}

	public Participate(String name, Team team) {
		this.name = name;
		team.addMember(this);
	}

	public String getName() {
		return name;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public boolean hasTeam() { return team != null; }

	public Player getPlayer() {
		return SpeedChallenge.getInstance().getServer().getPlayer(name);
	}

}
