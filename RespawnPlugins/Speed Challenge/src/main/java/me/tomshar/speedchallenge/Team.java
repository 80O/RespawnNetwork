package me.tomshar.speedchallenge;

import me.tomshar.speedchallenge.challenge.Challenge;
import me.tomshar.speedchallenge.util.Announcement;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 10/03/14.
 */
public class Team {

	private final String name;
	private final ChatColor teamColor;

	private Shrine shrine;
	private Challenge challenge;

	private List<Participate> members = new ArrayList<Participate>();

	public Team(String name, ChatColor teamColor) {
		this.name = name;
		this.teamColor = teamColor;
	}

	public Team(String name, ChatColor teamColor, List<Participate> members) {
		this.name = name;
		this.teamColor = teamColor;
		this.members = members;

		for(Participate member : members)
			member.setTeam(this);
	}

	public void addMember(Participate member) {
		Announcement.EVENT.send(member.getName() + " has joined the " + name + ".", this);
		member.setTeam(this);
		members.add(member);
	}

	public void removeMember(Participate member, boolean removed) {
		Announcement.EVENT.send(member.getName() + " has " + (removed ? "been removed" : "left") + " the " + name + ".", this);
		member.setTeam(null);
		members.remove(member);
	}

	public void removeAllMembers() {
		Announcement.EVENT.send("The " + name + " has removed all players.", this);

		for(Participate member : members)
			member.setTeam(null);

		members.clear();
	}

	public Participate getMember(int index) {
		return members.get(index);
	}

	public List<Participate> getMemebers() {
		return members;
	}

	public String getName() {
		return name;
	}

	public ChatColor getTeamColor() {
		return teamColor;
	}

	public Shrine getShrine() {
		return shrine;
	}

	public void setShrine(Shrine shrine) {
		this.shrine = shrine;
	}

	public Challenge getChallenge() {
		return challenge;
	}

	public void setChallenge(Challenge challenge) {
		this.challenge = challenge;
	}

}
