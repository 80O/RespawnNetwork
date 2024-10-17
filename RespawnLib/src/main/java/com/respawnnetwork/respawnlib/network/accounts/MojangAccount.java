package com.respawnnetwork.respawnlib.network.accounts;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.mojang.api.profiles.HttpProfileRepository;
import com.mojang.api.profiles.Profile;
import com.mojang.api.profiles.ProfileRepository;
import lombok.Getter;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the mojang account for players.
 *
 * @author TomShar
 * @author spaceemotion
 * @version 1.0.1
 */
public class MojangAccount {
    /** The logger for this helper class */
    public static final Logger LOGGER = Logger.getLogger("MojangAccount");

    /** The minecraft agent name */
    public static final String MINECRAFT_AGENT = "minecraft";

    /** Holds "name" to "id" results, this is a bidirectional map to allow reversed access */
    private static final BidiMap<String, String> CACHE = new DualHashBidiMap<>();

    private static final Gson GSON = new Gson();

    @Getter
    private final boolean valid;

    @Getter
    private String uuid;

    @Getter
    private final String name;

    /**
     * Creates a new mojang account by looking up the bukkit player.
     *
     * @param player The bukkit player
     */
    @SuppressWarnings("deprecation")
    public MojangAccount(Player player) {
        this(player.getName());

        if (uuid == null) {
            uuid = player.getUniqueId().toString().replace("-", "");
        }
    }

    /**
     * Creates a new mojang account by looking up a player name.
     *
     * @param name The player name to use for lookup
     */
    public MojangAccount(String name) {
        this(getPlayerIDFromName(name), name);
    }

    private MojangAccount(String uuid, String name) {
        this.valid = (uuid != null && name != null);
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Creates a new mojang account by looking up a player's UUID.
     *
     * @param id The UUID to use for lookup
     */
    public static MojangAccount fromUUID(String id) {
        return new MojangAccount(id, getPlayerNameFromID(id));
    }

    public static String toString(UUID uuid) {
        return uuid.toString().replace("-", "");
    }

    private static String getPlayerIDFromName(String name) {
        String uuid = CACHE.get(name);

        if (uuid != null) {
            return uuid;
        }

        ProfileRepository repository = new HttpProfileRepository(MINECRAFT_AGENT);
        Profile[] pages = repository.findProfilesByNames(name);

        if (pages.length != 1) {
            LOGGER.warning("API returned " + pages.length + " page(s) for name " + name);
            return null;
        }

        if (!pages[0].getName().equals(name)) {
            LOGGER.warning("Name mismatch for name " + name);
            return null;
        }

        String id = pages[0].getId();
        CACHE.put(name, id);

        return id;
    }

    private static String getPlayerNameFromID(String uuid) {
        String name = CACHE.getKey(uuid);

        if (name != null) {
            return name;
        }

        try {
            // Make request
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid);
            JsonObject json = GSON.fromJson(new JsonReader(new InputStreamReader(url.openStream())), JsonElement.class);

            if (json == null) {
                throw new IllegalStateException("No profile returned, user might not be premium");
            }

            String id = json.get("id").getAsString();

            // Get page
            if (!uuid.equalsIgnoreCase(id)) {
                throw new IllegalStateException("UUID mismatch, got " + id);
            }

            name = json.get("name").getAsString();
            CACHE.put(name, uuid);

        } catch (IllegalStateException ex) {
            LOGGER.log(Level.WARNING, "Could not get player name from UUID " + uuid, ex);

        } catch (MalformedURLException ex) {
            LOGGER.log(Level.WARNING, "Could not create URL for UUID" + uuid, ex);

        } catch (NullPointerException | IOException ex) {
            // The null pointer can happen if mojang decides to change their layout
            // and we try to access invalid fields, so we also log them as read errors

            LOGGER.log(Level.WARNING, "Could not read JSON for UUID" + uuid, ex);
        }

        return name;
    }

}
