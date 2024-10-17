package me.tomshar.speedchallenge.util;

import me.tomshar.speedchallenge.SpeedChallenge;
import me.tomshar.speedchallenge.Team;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Tom on 10/03/14.
 */
public enum Announcement {
	NORMAL("§7%s"),
	WARNING("§6%s"),
	DANGER("§4%s"),
	INFORMATION("§b%s"),
	EVENT("§l%s", true);

	private final SpeedChallenge instance = SpeedChallenge.getInstance();
	private final String format;
	private final boolean team_required;

	private List<String> recipients = new ArrayList<>();

	private Map<String, Integer> groupedMsgs = new HashMap<>();
	private Map<String, Integer> groupedMsgsId = new HashMap<>();

	private Announcement(String format) {
		this.format = format;
		this.team_required = false;
	}

	private Announcement(String format, boolean team_required) {
		this.format = format;
		this.team_required = team_required;
	}

	public void send(String msg) {
		if(team_required) return;

		if(recipients.isEmpty()) {
			instance.getServer().broadcastMessage(String.format(format, msg));
		} else {
			for(String name : recipients) {
				instance.getServer().getPlayer(name).sendMessage(String.format(format, msg));
			}
		}
	}

	public void send(String msg, Team team) {
		if(recipients.isEmpty()) {
			instance.getServer().broadcastMessage(team.getTeamColor() + String.format(format, msg));
		} else {
			for(String name : recipients) {
				instance.getServer().getPlayer(name).sendMessage(team.getTeamColor() + String.format(format, msg));
			}
		}
	}

	public void sendGrouped(String msg, int groupBy, Team team) {
		if(!groupedMsgs.containsKey(msg)) {
			groupedMsgs.put(msg, groupBy);
			int id = new GroupAnnouncements(msg, team).runTaskLater(SpeedChallenge.getInstance(), 60).getTaskId();
			groupedMsgsId.put(msg, id);
		} else {
			SpeedChallenge.getInstance().getServer().getScheduler().cancelTask(groupedMsgsId.get(msg)); // cancel task
			groupedMsgs.put(msg, groupedMsgs.get(msg) + groupBy);
			int id = new GroupAnnouncements(msg, team).runTaskLater(SpeedChallenge.getInstance(), 60).getTaskId();
			groupedMsgsId.put(msg, id);
		}
	}

	public Announcement setRecipient(String recipient) {
		this.recipients = Arrays.asList(recipient);
		return this;
	}

	public Announcement setRecipients(List<String> recipients) {
		this.recipients = recipients;
		return this;
	}

	class GroupAnnouncements extends BukkitRunnable {

		private final String msg;
		private final Team team;

		public GroupAnnouncements(String msg, Team team) {
			this.msg = msg;
			this.team = team;
		}

		@Override
		public void run() {
			EVENT.send(msg.replace("{groupBy}", Integer.toString(groupedMsgs.get(msg))), team);

			groupedMsgs.remove(msg);
			groupedMsgsId.remove(msg);
		}

	}

}
