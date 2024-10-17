package com.respawnnetwork.respawnlib.utils;

import org.bukkit.ChatColor;

import java.util.List;

/**
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public final class StringUtils {

    private StringUtils() {
        // Private constructor
    }


    /**
     * Implodes the given collection in "proper english". Examples:
     *
     * <ol>
     *     <li>[A] will turn to "A"</li>
     *     <li>[A, B] will turn to "A and B"</li>
     *     <li>[A, B, C] will turn to "A, B and C"</li>
     * </ol>
     *
     * @param entries The entries to implode
     * @return The imploded string
     */
    public static String implodeProperEnglish(List<String> entries) {
        if (entries == null || entries.isEmpty()) {
            return "";
        }

        String first = entries.get(0);

        if (entries.size() == 1) {
            return first;
        }

        StringBuilder builder = new StringBuilder(first);

        for (int i = 1; i < entries.size(); i++) {
            if (i == entries.size() - 1) {
                builder.append(" and ");

            } else {
                builder.append(", ");
            }

            builder.append(entries.get(i));
        }

        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

}
