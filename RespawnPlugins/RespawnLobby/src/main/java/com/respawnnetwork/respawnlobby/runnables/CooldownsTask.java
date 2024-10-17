package com.respawnnetwork.respawnlobby.runnables;

import com.respawnnetwork.respawnlobby.RespawnLobby;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Represents a task that clears all show/hide players cooldowns after a certain time
 */
public class CooldownsTask extends LobbyRunnable {
    private final TMap<UUID, Integer> cooldownsMap;
    private int hideCooldown;


    /**
     * Creates a new cooldown task.
     *
     * @param plugin The lobby plugin instance
     */
    public CooldownsTask(RespawnLobby plugin) {
        super(plugin);

        cooldownsMap = new THashMap<>();
    }

    @Override
    public void loadConfig(ConfigurationSection section) {
        hideCooldown = section.getInt("hideCooldown", 30);
    }

    @Override
    public void run() {
        if (cooldownsMap.isEmpty()){
            return;
        }

        for (UUID UUID: new LinkedList<>(cooldownsMap.keySet())) {
            if (cooldownsMap.get(UUID) < hideCooldown) {
                cooldownsMap.put(UUID, cooldownsMap.get(UUID) + 1);
                continue;
            }

            if (cooldownsMap.get(UUID) >= hideCooldown) {
                cooldownsMap.remove(UUID);
            }
        }
    }

    /**
     * Adds a UUID to the cooldown list.
     *
     * @param UUID the UUID to add
     */
    public void addUUID(UUID UUID) {
        cooldownsMap.put(UUID, 0);
    }

    /**
     * Removes a UUID from the cooldown list.
     *
     * @param UUID the UUID to remove
     */
    public void removeUUID(UUID UUID) {
        cooldownsMap.remove(UUID);
    }

    /**
     * Indicates whether or not the specified UUID is on cooldown.
     *
     * @param UUID The UUID to check
     * @return True if it is, false if not
     */
    public boolean isOnCooldown(UUID UUID) {
        return cooldownsMap.containsKey(UUID);
    }

}
