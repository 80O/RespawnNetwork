package com.respawnnetwork.respawnlib.gameapi.modules.team;

import com.respawnnetwork.respawnlib.bukkit.Item;
import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.lang.Nameable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scoreboard.Score;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents a team in a game.
 *
 * @author spaceemotion
 * @version 1.0.1
 */
@RequiredArgsConstructor
public class Team implements Displayable, Nameable {
    /** The players in this team */
    @Getter
    private final List<GamePlayer> players = new LinkedList<>();

    /** The display name for the team */
    @Getter
    private final String displayName;

    /** The internal name for the team */
    @Getter
    private final String name;

    /** The team color */
    @Getter
    private final TeamColor color;

    /** The minimum number of players required for this team */
    @Getter
    private final int minPlayers;

    /** The maximum number of players allowed for this team */
    @Getter
    private final int maxPlayers;

    /** The inventory for team members */
    @Getter
    private final List<ItemStack> inventory = new LinkedList<>();

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private int defaultScore;

    /** The team's spawn location */
    @Getter
    @Setter
    private Location spawnLocation;

    /** The scoreboard team */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private org.bukkit.scoreboard.Team team;

    /** The scoreboard score object */
    private Score score;


    /**
     * Returns the name of this team as it would appear on the scoreboard.
     * <p />
     * This includes the color code plus the actual display name.
     *
     * @return The scoreboard name of the team
     */
    public String getScoreboardName() {
        return getColor().getChatColor() + getDisplayName();
    }

    /**
     * Returns the score of this team.
     *
     * @return The scoreboard score
     */
    public int getScore() {
        if (score == null) {
            return 0;
        }

        return score.getScore();
    }

    /**
     * Sets the score of this team.
     *
     * @param number The new scoreboard score
     */
    public void setScore(int number) {
        if (score == null) {
            return;
        }

        score.setScore(number);
    }

    void setScore(Score score) {
        this.score = score;
    }

    public void increaseScore(int increase) {
        if (score == null) {
            return;
        }

        score.setScore(score.getScore() + increase);
    }

    public void decreaseScore(int increase) {
        if (score == null) {
            return;
        }

        score.setScore(score.getScore() - increase);
    }

    /**
     * Resets the inventory of the given player.
     * <p />
     * This will first clear the player's inventory, then add the team's
     * inventory to the inventory of the player. Armor items will automatically
     * get equipped by this.
     *
     * @param player The player to reset the inventory for
     */
    public void resetInventory(Player player) {
        if (player == null) {
            return;
        }

        PlayerInventory playerInventory = player.getInventory();
        playerInventory.clear();

        // Add items
        for (ItemStack item : getInventory()) {
            // Auto-equip player armor
            int armorSlot = Item.getArmorSlot(item.getType());

            if (armorSlot >= 0) {
                ItemStack[] contents = playerInventory.getArmorContents();
                contents[armorSlot] = item;

                playerInventory.setArmorContents(contents);

            } else {
                // Regularly add item
                playerInventory.addItem(item.clone());
            }
        }
    }

    /**
     * Resets the team score to the default score.
     * <p />
     * Same as calling <code>setScore(getDefaultScore())</code>.
     */
    public void resetScore() {
        setScore(getDefaultScore());
    }

}
