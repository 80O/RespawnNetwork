package com.respawnnetwork.respawnlib.gameapi.modules.team;

import lombok.Data;
import org.bukkit.ChatColor;
import org.bukkit.Color;

/**
 * Holds the different color types for a team.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
@Data
public class TeamColor {
    /** The number of available font colors (excluding font styles like italic) */
    public static final short NUM_COLORS = 16;

    /** A list of valid color codes */
    public static final Color[] COLOR_CODES;

    /** An array containing default team colors */
    public static final TeamColor[] DEFAULTS;

    // -------------------------------------------------------------------------------
    // Calculate native minecraft RGB colors
    //
    // This code calculates the colors as they appear on the vanilla minecraft client.
    // As notch didn't just enter a row of hex values, they are completely calculate-
    // able.
    //
    // Since bukkit weirdly enough changed the order of the colors, this will not
    // calculate the colors in the order as they would on the client (R and B have
    // been switched).
    //
    // Code (c) 2012-2014 by spaceemotion - from cILIA, the Catharos Interface library
    // Used with permission from original author

    static {
        COLOR_CODES = new Color[NUM_COLORS];

        for (int i = 0; i < COLOR_CODES.length; i++) {
            int j = (i >> 3 & 1) * 85;
            int r = (i & 1) * 170 + j;
            int g = (i >> 1 & 1) * 170 + j;
            int b = (i >> 2& 1) * 170 + j;

            if (i == 6) {
                r += 85;
            }

            COLOR_CODES[i] = Color.fromBGR(r, g, b);
        }
    }

    // -------------------------------------------------------------------------------

    public static final TeamColor RED           = new TeamColor(ChatColor.RED);
    public static final TeamColor BLUE          = new TeamColor(ChatColor.BLUE);
    public static final TeamColor GREEN         = new TeamColor(ChatColor.GREEN);
    public static final TeamColor YELLOW        = new TeamColor(ChatColor.YELLOW);
    public static final TeamColor LIGHT_PURPLE  = new TeamColor(ChatColor.LIGHT_PURPLE);
    public static final TeamColor GRAY          = new TeamColor(ChatColor.GRAY);

    public static final TeamColor DARK_RED      = new TeamColor(ChatColor.DARK_RED);
    public static final TeamColor AQUA          = new TeamColor(ChatColor.AQUA);
    public static final TeamColor DARK_GREEN    = new TeamColor(ChatColor.DARK_GREEN);
    public static final TeamColor GOLD          = new TeamColor(ChatColor.GOLD);
    public static final TeamColor DARK_PURPLE   = new TeamColor(ChatColor.DARK_PURPLE);
    public static final TeamColor DARK_GRAY     = new TeamColor(ChatColor.DARK_GRAY);

    static {
        // Add default team colors, first normal versions, then alternative versions
        DEFAULTS = new TeamColor[] {
                RED, BLUE, GREEN, YELLOW, LIGHT_PURPLE, GRAY,
                DARK_RED, AQUA, DARK_GREEN, GOLD, DARK_PURPLE, DARK_GRAY
        };
    }

    private final ChatColor chatColor;

    private Color color;


    public TeamColor(ChatColor chatColor) throws IllegalArgumentException {
        this(chatColor, getColor(chatColor));
    }

    public TeamColor(ChatColor chatColor, Color color) {
        if (!chatColor.isColor()) {
            throw new IllegalArgumentException("Given chat color is not a valid color");
        }

        this.chatColor = chatColor;
        this.color = color;
    }

    private static Color getColor(ChatColor chatColor) throws IllegalArgumentException {
        // Get corresponding chat color
        int index = chatColor.ordinal();
        if (index >= NUM_COLORS) {
            throw new IllegalArgumentException("Given chat color is not within valid bounds");
        }

        return COLOR_CODES[index];
    }

}
