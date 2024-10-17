package com.respawnnetwork.respawnlib.network.messages;

import gnu.trove.map.hash.THashMap;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Map;

/**
 * Basic formatted messaging API.
 *
 * @author TomShar
 * @author spaceemotion
 * @version 1.0.1
 * @since 1.0 (refactored in 1.0.1)
 */
public class Message implements MessageCreator {
    /** Default announcements */
    public static final Message ANNOUNCE = new Message("&b&l%s");

    /** Custom messages */
    public static final Message CUSTOM = new Message("%s");

    /** Dangerous (error) messages */
    public static final Message DANGER = new Message("&4&l[ERROR]&r&4 %s");

    /** Informational messages */
    public static final Message INFO = new Message("[&6&lINFO&r]&6 %s");

    /** Warnings */
    public static final Message WARNING = new Message("&e%s");

    static final Map<String, String> DEFAULTS = new THashMap<>();
    static boolean addOnlineByDefault = true;

    /** The format for the message */
    private final String format;


    /**
     * Creates a new message with the given format.
     *
     * @param format The format of the message
     */
    public Message(String format) {
        this.format = format;
    }

    /**
     * Returns a new message creator instance.
     *
     * @return A new message builder
     */
    public MessageCreator create() {
        return builder();
    }

    @Override
    public MessageCreator receivers(CommandSender... list) {
        return builder().receivers(list);
    }

    @Override
    public MessageCreator receivers(List<CommandSender> list) {
        return builder().receivers(list);
    }

    @Override
    public MessageCreator format(String format) {
        return builder().format(format);
    }

    @Override
    public MessageCreator provide(String key, Object value) {
        return builder().provide(key, value);
    }

    @Override
    public MessageCreator provide(Map<String, Object> data) {
        return builder().provide(data);
    }

    @Override
    public MessageCreator setDisplay(MessageDisplay display) {
        return builder().setDisplay(display);
    }

    @Override
    public void send(String message) {
        builder().send(message);
    }

    @Override
    public void send(String message, Object... args) {
        builder().send(message, args);
    }

    @Override
    public void sendKey(String message) {
        builder().sendKey(message);
    }

    @Override
    public void sendKey(String message, Object... args) {
        builder().sendKey(message, args);
    }

    @Override
    public void send(CommandSender sender, String message) {
        builder().send(sender, message);
    }

    @Override
    public void send(CommandSender sender, String message, Object... args) {
        builder().send(sender, message, args);
    }

    @Override
    public void sendKey(CommandSender sender, String message) {
        builder().sendKey(sender, message);
    }

    @Override
    public void sendKey(CommandSender sender, String message, Object... args) {
        builder().sendKey(sender, message, args);
    }

    @Override
    public String parse(String message) {
        return builder().parse(message);
    }

    @Override
    public String parseKey(String message) {
        return builder().parseKey(message);
    }

    /**
     * Creates a new message builder instance using the specified format.
     *
     * @return The new message builder
     */
    protected MessageBuilder builder() {
        return new MessageBuilder(format);
    }


    /**
     * Adds a default message value.
     *
     * @param key The message key
     * @param value The default message content
     */
    public static void addDefault(String key, String value) {
        DEFAULTS.put(key, value);
    }

    /**
     * Adds a map of default values to use for messaging.
     *
     * @param defaults The default values to add
     */
    public static void addDefaults(Map<String, String> defaults) {
        DEFAULTS.putAll(defaults);
    }

    /**
     * Clears all stored default values for messages.
     */
    public static void clearDefaults() {
        DEFAULTS.clear();
    }

    /**
     * Indicates whether or not we're adding all online players by default.
     * <p />
     * <b>This only refers to an empty participation list.</b> A message with a custom list
     * of receivers will not be filled.
     *
     * @return True if we add online players by default, false if not
     */
    public static boolean addOnlineByDefault() {
        return addOnlineByDefault;
    }

    /**
     * Sets the "add online by default" feature for messages.
     *
     * @param addOnlineByDefault True if we add online players by default, false if not
     * @see #addOnlineByDefault()
     */
    public static void setAddOnlineByDefault(boolean addOnlineByDefault) {
        Message.addOnlineByDefault = addOnlineByDefault;
    }

}
