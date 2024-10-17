package com.respawnnetwork.respawnlib.network.signs;

import com.respawnnetwork.respawnlib.lang.Providable;
import gnu.trove.map.hash.THashMap;
import org.bukkit.ChatColor;
import org.bukkit.block.Sign;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Represents a sign builder to build signs quickly, by using variable parsing.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
public class SignBuilder implements Providable<SignBuilder> {
    public static final int LINE_COUNT = 4;
    public static final int LINE_LIMIT = 15;

    /** Provided data for message */
    private final Map<String, Object> data;

    /** The lines on the sign */
    private final String[] lines;

    /** The sign we're changing */
    private final Collection<Sign> signs;


    /**
     * Creates a new sign builder with no initial signs.
     */
    public SignBuilder() {
        this.signs = new LinkedList<>();

        this.data = new THashMap<>();
        this.lines = new String[LINE_COUNT];
    }

    /**
     * Creates a new sign builder for the specified sign.
     *
     * @param sign The sign to change the data on
     */
    public SignBuilder(Sign sign) {
        this();

        add(sign);
    }

    /**
     * Sets the content for the specified sign.
     * <p />
     * This will remove every character that exceeds the line limit.
     *
     * @param line The line on the sign
     * @param text The text content
     * @return The sign creator, for method chaining
     */
    public SignBuilder line(int line, String text) {
        if (line >= 0 && line < LINE_COUNT) {
            this.lines[line] = text;
        }

        return this;
    }

    /**
     * Sets the full content for the specified sign.
     * <p />
     * This will override everything that has been set before, plus
     * remove every character that exceeds the line limit.
     *
     * @param lines The lines on the sign
     * @return The sign creator, for method chaining
     */
    public SignBuilder lines(String... lines) {
        int i = 0;

        for(String line : lines) {
            this.lines[i++] = line;

            if (i >= LINE_COUNT) {
                break;
            }
        }

        return this;
    }

    @Override
    public SignBuilder provide(String key, Object value) {
        this.data.put(key, value);

        return this;
    }

    @Override
    public SignBuilder provide(Map<String, Object> data) {
        this.data.putAll(data);

        return this;
    }

    /**
     * Adds a sign to the update process.
     *
     * @param sign The sign to change the data on
     * @return This sign builder to allow method chaining
     */
    public SignBuilder add(Sign sign) {
        signs.add(sign);

        return this;
    }

    /**
     * Adds a collection of signs to the update process.
     *
     * @param signs The signs to add
     * @return This sign builder to allow method chaining
     */
    public SignBuilder add(Collection<Sign> signs) {
        for (Sign sign : signs) {
            add(sign);
        }

        return this;
    }

    /**
     * Applies the changes to the linked sign.
     * <p />
     * A world update with out physics will be executed automatically. If no signs has been added
     * to this builder, this will do nothing.
     */
    public void apply() {
        if (signs.isEmpty()) {
            return;
        }

        String[] formattedLines = new String[LINE_COUNT];

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line == null) {
                continue;
            }

            String signLine = ChatColor.translateAlternateColorCodes(COLOR_CODE, line);

            // Parse provided data
            for(Map.Entry<String, Object> entry : data.entrySet()) {
                signLine = signLine.replace(VARIABLES_OPEN + entry.getKey() + VARIABLES_CLOSE, entry.getValue().toString());
            }

            // Add text to formatted lines
            int length = signLine.length();
            formattedLines[i] = signLine.substring(0, length < LINE_LIMIT ? length : LINE_LIMIT);
        }

        // Apply the new sign text
        for (Sign sign : signs) {
            for (int line = 0; line < formattedLines.length; line++) {
                sign.setLine(line, formattedLines[line]);
            }

            // Update sign block
            sign.update(true, false);
        }
    }

}
