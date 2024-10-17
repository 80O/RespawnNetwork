package com.respawnnetwork.respawnlib.lang;

import java.util.Map;

/**
 * Represents an interface for objects that can be provided with data.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public interface Providable<P extends Providable> {
    /** The alternative color code we use for formatting messages */
    static final char COLOR_CODE = '&';

    static final char VARIABLES_OPEN = '{';
    static final char VARIABLES_CLOSE = '}';

    /**
     * Adds a variable to replace when sending a message.
     *
     * @param key The key of the variable
     * @param value The value to replace it with
     * @return A MessageCreator instance for method chaining
     */
    P provide(String key, Object value);

    /**
     * Adds a map of variables to replace when sending a message.
     *
     * @param data The data to add
     * @return A MessageCreator instance for method chaining
     */
    P provide(Map<String, Object> data);

}
