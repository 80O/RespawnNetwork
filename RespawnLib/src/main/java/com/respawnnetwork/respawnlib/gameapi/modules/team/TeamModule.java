package com.respawnnetwork.respawnlib.gameapi.modules.team;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.ScoreModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.events.PlayerJoinsTeamEvent;
import com.respawnnetwork.respawnlib.network.scoreboard.Scoreboards;
import gnu.trove.map.hash.THashMap;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;
import java.util.logging.Level;

/**
 * Represents a module that adds team-based functionality to the game.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
public class TeamModule<G extends Game> extends GameModule<G> implements Listener {
    private static final String SKIP = " Skipping Team...";

    private final Map<String, Team> teams;
    private ScoreModule scoreModule;

    @Getter
    private boolean[] armor;


    public TeamModule(G game) {
        super(game);

        this.teams = new THashMap<>();
    }

    @Override
    protected boolean onEnable() {
        // Try to get score module
        GameModule module = getGame().getModule(ScoreModule.class);

        if (module != null) {
            boolean loaded = module.isLoaded();

            if (loaded) {
                getLogger().info("Scoreboard module found and loaded, adding scoreboard functionality to teams!");
                scoreModule = (ScoreModule) module;

            } else {
                getLogger().warning(
                        "Scoreboard module found but not loaded, please re-order load process to use it for teams!"
                );
            }
        }

        // Load config
        int count = loadConfig();

        if (count == 0) {
            getLogger().warning("No teams specified! Players will be kicked instantly!");

        } else {
            getLogger().info("Loaded " + count + (count == 1 ? " Team!" : " Teams!"));
            loadInventories();
        }

        return true;
    }

    public void assignTeamsToPlayers() {
        // First we reset the scores for all teams
        if (scoreModule != null) {
            Scoreboards scoreboards = scoreModule.getScoreboards();

            // scoreboards.clear();
            scoreboards.assignToAll();
            scoreboards.displaySlot(DisplaySlot.SIDEBAR);
        }

        // Then we assign all game players to a team
        // ----------------------------------------------------------------
        // We first create a shuffled player collection and then iterate
        // over until no team is left
        // If the armor toggle is enabled, this will also add (and replace)
        // the player's armor, using dyed leather armor

        List<Team> teamList = new ArrayList<>(teams.values());

        // Reset all team scores
        for (Team team : teamList) {
            team.resetScore();
        }

        List<GamePlayer> gamePlayers = Arrays.asList(getGame().getPlayers());

        Team team;
        int teamCount = teams.size();

        for (int i = 0, max = gamePlayers.size(); i < max; i++) {
            GamePlayer player = gamePlayers.get(i);
            if (player == null) {
                continue;
            }

            team = teamList.get(i % teamCount);
            if (team == null || team.getPlayers().size() == team.getMaxPlayers()) {
                getLogger().info("Setting " + player.getName() + " to spectator mode since team is full");
                player.setSpectator(true);
                continue;
            }

            // Add to team
            addPlayerToTeam(player, team);
        }
    }

    /**
     * Helper method to add a player properly to a team.
     *
     * @param player The player to add
     * @param team The team to add the player to
     * @return True on success, false if not
     */
    public boolean addPlayerToTeam(GamePlayer player, Team team) {
        if (player == null || team == null) {
            return false;
        }

        PlayerJoinsTeamEvent joinsTeamEvent = new PlayerJoinsTeamEvent(getGame(), player, team);
        callEvent(joinsTeamEvent);

        if (!joinsTeamEvent.isCancelled()) {
            getLogger().info("Adding " + player.getName() + " to " + team.getDisplayName());

            player.setSpectator(false);

            team.getPlayers().add(player);

            if (team.getTeam() != null) {
                OfflinePlayer offlinePlayer = player.getPlayer();

                if (offlinePlayer == null) {
                    return false;
                }

                team.getTeam().addPlayer(offlinePlayer);
            }

            // Teleport to spawn location
            if (team.getSpawnLocation() != null) {
                player.teleportTo(team.getSpawnLocation());
            }

            // Reset inventory and give the starter items
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer != null) {
                team.resetInventory(bukkitPlayer);
            }

            return true;
        }

        return false;
    }

    public boolean removePlayerFromTeam(GamePlayer player, Team team) {
        if (player == null || team == null) {
            return false;
        }

        getLogger().info("Removing " + player.getName() + " from " + team.getDisplayName());
        return team.getPlayers().remove(player);
    }

    @EventHandler
    public void onMoveArmorItems(InventoryClickEvent event) {
        if (event.isCancelled()) {
            return;
        }

        // Ignore non-armor slots
        if (event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        boolean global = !event.getMessage().startsWith("*");
        GamePlayer gamePlayer = getGame().getPlayer(event.getPlayer());
        Team team = getTeam(gamePlayer);

        if (team != null) {
            // If it's a non-global message, remove all players and then only add those in your team
            if (!global) {
                event.setMessage(event.getMessage().substring(1));
                event.getRecipients().clear();

                for (GamePlayer player : team.getPlayers()) {
                    event.getRecipients().add(player.getPlayer());
                }
            }

            // Set the chat format
            String prefix = global ? "[G] ": "[T] ";
            event.setFormat(prefix + team.getTeam().getPrefix() +  "%1$s" + team.getTeam().getSuffix() + ": %2$s");
        }
    }

    private int loadConfig() {
        if (getConfig().isConfigurationSection("armor")) {
            ConfigurationSection section = getConfig().getConfigurationSection("armor");

            setArmor(
                    section.getBoolean("helmet", true),
                    section.getBoolean("chestplate", true),
                    section.getBoolean("leggings", true),
                    section.getBoolean("boots", true)
            );

        } else if (getConfig().getBoolean("armor", true)) {
            setArmor(true);
        }

        ConfigurationSection list = getConfig().getConfigurationSection("list");

        int count = 0;
        for (String id : list.getKeys(false)) {
            if (!list.isConfigurationSection(id)) {
                getLogger().warning("Invalid team config for entry '" + id + "'!" + SKIP);
                continue;
            }

            ConfigurationSection section = list.getConfigurationSection(id);

            // Get properties
            String displayName = section.getString("name", id);

            String chatColorString = section.getString("chatColor");
            TeamColor color = null;

            if (chatColorString != null) {
                try {
                    // We set our own color, is it instead
                    color = new TeamColor(ChatColor.valueOf(chatColorString.replace(' ', '_').toUpperCase()));

                } catch (IllegalArgumentException ex) {
                    getLogger().log(Level.WARNING, "Invalid team color provided: " + chatColorString, ex);
                    continue;
                }

            } else {
                // Go through the chat colors
                for (ChatColor entry : ChatColor.values()) {
                    if (!entry.name().equalsIgnoreCase(id)) {
                        continue;
                    }

                    color = new TeamColor(entry);
                }
            }

            if (color == null && count < TeamColor.DEFAULTS.length) {
                // Use one of the default colors, if available
                color = TeamColor.DEFAULTS[count];
            }

            if (color == null) {
                getLogger().warning("We either ran out of default colors and /or no default color for Team '"
                        + id + "' specified!" + SKIP);
                continue;
            }

            // Get custom color if specified
            String colorString = section.getString("color");
            if (colorString != null) {
                if (colorString.startsWith("#") && colorString.length() == 7) {
                    color.setColor(Color.fromRGB(
                            Integer.valueOf(colorString.substring(1, 3), 16),
                            Integer.valueOf(colorString.substring(3, 5), 16),
                            Integer.valueOf(colorString.substring(5, 7), 16)));

                } else {
                    getLogger().warning("Invalid color string: " + colorString + ", should be #RRGGBB");
                }
            }

            int minPlayers = section.getInt("players");
            if (minPlayers == 0) {
                getLogger().warning("Team size for team " + displayName + " cannot be 0!" + SKIP);
                continue;
            }

            int maxPlayers = section.getInt("maxPlayers", minPlayers);
            if (maxPlayers < minPlayers) {
                getLogger().warning("Maximum team size for team " + displayName + " cannot be under minimum!" + SKIP);
                continue;
            }

            // Create team
            Team team = new Team(displayName, id, color, minPlayers, maxPlayers);
            teams.put(team.getName(), team);

            // Load spawn location
            if (section.isConfigurationSection("spawnLocation")) {
                team.setSpawnLocation(new Location(
                        getGame().getMap().getWorld(),
                        section.getConfigurationSection("spawnLocation").getValues(false)
                ));
            }

            // Load scoreboard features if available
            loadScoreboardFeatures(team, section);

            // Increase counter
            getLogger().info("Loaded team '" + displayName + "' successfully!");
            count++;
        }

        return count;
    }

    private void loadInventories() {
        Map<String, List<ItemStack>> inventories = new THashMap<>();

        // Create inventories
        ConfigurationSection section = getConfig().getConfigurationSection("inventories");
        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key)) {
                getLogger().warning(key + " is not a valid inventory config!");
                continue;
            }

            ConfigurationSection config = section.getConfigurationSection(key);
            List<ItemStack> inventory = new LinkedList<>();

            String parent = config.getString("parent");
            boolean hasParent = false;

            if (parent != null) {
                hasParent = true;

                List<ItemStack> parentInventory = inventories.get(parent);

                if (parentInventory != null) {
                    inventory.addAll(parentInventory);

                 } else {
                    getLogger().warning("No parent inventory with name '" + parent + "' found!");
                }
            }

            // Inventories don't really need items if they have a parent
            if (config.isList("items")) {
                Item.parseInventory(getLogger(), config.getList("items"), inventory);

            } else if (!hasParent) {
                getLogger().warning("Inventory has no parent and no items:" + key);
                continue;
            }

            inventories.put(key, inventory);
        }

        // Add items to teams
        for (Team team : getTeams()) {
            List<ItemStack> inventory = inventories.get(team.getName());

            // Add all items if the team has an inventory
            if (inventory != null) {
                team.getInventory().addAll(inventory);
            }

            // Also add armor items, if set in config
            if (armor != null) {
                List<ItemStack> armorItems = new ArrayList<>();
                Material[] materials = new Material[] {
                        Material.LEATHER_HELMET,
                        Material.LEATHER_CHESTPLATE,
                        Material.LEATHER_LEGGINGS,
                        Material.LEATHER_BOOTS
                };

                for (int i = materials.length - 1; i >= 0; i--) {
                    if (!armor[i]) {
                        continue;
                    }

                    armorItems.add(Item.dyeLeatherArmor(new ItemStack(materials[i]), team.getColor().getColor()));
                }

                // Add them to the team's inventory
                team.getInventory().addAll(armorItems);
            }
        }
    }

    private void loadScoreboardFeatures(Team team, ConfigurationSection section) {
        if (scoreModule == null) {
            return;
        }

        int startScore = section.getInt("startScore", 0);
        if (startScore == 0) {
            getLogger().warning("Start score equals zero! Has it been set correctly?");

        } else {
            team.setDefaultScore(startScore);
        }

        // Get or create scoreboard team
        Scoreboard scoreboard = scoreModule.getScoreboards().getScoreboard();
        org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.getName());

        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(team.getName());
        }

        scoreboardTeam.setDisplayName(team.getDisplayName());
        scoreboardTeam.setAllowFriendlyFire(section.getBoolean("friendlyFire", false));
        scoreboardTeam.setCanSeeFriendlyInvisibles(section.getBoolean("canSeeFriendlyInvisibles", true));
        scoreboardTeam.setPrefix(team.getColor().getChatColor().toString());
        scoreboardTeam.setSuffix(ChatColor.RESET.toString());

        // Use scoreboard name for the team
        Score score = scoreModule.setScore(team.getScoreboardName(), startScore);

        // Assign to team
        team.setTeam(scoreboardTeam);
        team.setScore(score);
        team.resetScore();
    }

    public Team getTeam(String name) {
        return teams.get(name);
    }

    public Team getTeam(GamePlayer player) {
        if (player == null) {
            return null;
        }

        for (Team team : teams.values()) {
            if (!team.getPlayers().contains(player)) {
                continue;
            }

            return team;
        }

        return null;
    }

    public Collection<Team> getTeams() {
        return teams.values();
    }

    public void setArmor(boolean armor) {
        setArmor(armor, armor, armor, armor);
    }

    public void setArmor(boolean helmet, boolean chestplate, boolean leggings, boolean boots) {
        this.armor = new boolean[] {helmet, chestplate, leggings, boots};
    }

    @Override
    public String getDisplayName() {
        return "Teams";
    }

    @Override
    public String getName() {
        return "teams";
    }

}
