package net.respawn.havok;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Tom on 19/03/14.
 */
public class HPlayer {

	private final UUID UUID;
	private Weapon currentWeapon;
	private long lastWeaponUseTime;
	private int kills = 0;
	private int deaths;

	public HPlayer(UUID UUID) {
		this.UUID = UUID;
	}

    public UUID getUniqueId() {

        return UUID;
    }

    public int getKills() {
		return kills;
	}

	public void incrementKills() {
		this.kills++;
	}

	public int getDeaths() {
		return deaths;
	}

	public void incrementDeaths() {
		this.deaths++;
	}

	public Weapon getCurrentWeapon() {
		return currentWeapon;
	}

	public void setCurrentWeapon(Weapon currentWeapon) {
		this.currentWeapon = currentWeapon;
	}

	public Player getPlayer() {
		return Bukkit.getPlayer(UUID);
	}

	public long getLastWeaponUseTime() {
		return lastWeaponUseTime;
	}

	public void setLastWeaponUseTime(long lastWeaponUseTime) {
		this.lastWeaponUseTime = lastWeaponUseTime;
	}
}
