package com.respawnnetwork.respawnlib.network.messages;

import com.respawnnetwork.respawnlib.lang.Providable;
import com.respawnnetwork.respawnlib.lang.Receivable;
import org.bukkit.command.CommandSender;

/**
 * An interface for a basic message creator to build messages.
 *
 * @author TomShar
 * @author spaceemotion
 * @version 1.0.1
 */
public interface MessageCreator extends Providable<MessageCreator>, Receivable<MessageCreator> {

    /**
     * Sets the format of this message.
     * <p />
     * When using the send method we will replace the first occurrence of "%s" in the
     * format with the message content.
     *
     * @param format The new message format
     * @return A MessageCreator instance for method chaining
     */
    MessageCreator format(String format);

    /**
     * Sets the display type for this message.
     *
     * @param display The display "slot"
     * @return A MessageCreator instance for method chaining
     * @since 1.0.1
     * @see com.respawnnetwork.respawnlib.network.messages.MessageDisplay
     */
    MessageCreator setDisplay(MessageDisplay display);

    /**
     * Parses the message and returns the final output.
     * This does not send the message!
     *
     * @param message The message to use
     * @return The parsed message
     */
    String parse(String message);

    /**
     * Parses the message key and returns the final output.
     * This does not send the message!
     *
     * @param message The message key to use
     * @return The parsed message
     */
    String parseKey(String message);

    /**
     * Sends a message to all receivers.
     *
     * @param message The message to send
     */
    void send(String message);

    /**
     * Parses the given arguments and sends the message to all receivers.
     *
     * @param message The message to send
     * @param args The arguments to parse
     */
    void send(String message, Object... args);

    /**
     * Parses the given arguments and sends the message to all receivers.
     * <p />
     * This uses a key instead of a real message. If the message could not be found, a
     * default content will be generated.
     *
     * @param message The message to send
     */
    void sendKey(String message);

    /**
     * Parses the given arguments and sends the message to all receivers.
     * <p />
     * This uses a key instead of a real message. If the message could not be found, ta
     * default content will be generated.
     *
     * @param message The message to send
     * @param args The arguments to parse
     */
    void sendKey(String message, Object... args);

    /**
     * Sends a message to all receivers plus the given command sender.
     *
     * @param sender The command sender to add
     * @param message The message to send
     */
    void send(CommandSender sender, String message);

    /**
     * Parses the given arguments and sends a message to all receivers plus the
     * given command sender.
     *
     * @param sender The command sender to add
     * @param message The message to send
     * @param args The arguments to parse
     */
    void send(CommandSender sender, String message, Object... args);

    /**
     * Sends a message to all receivers plus the given command sender.
     * <p />
     * This uses a key instead of a real message. If the message could not be found, a
     * default content will be generated.
     *
     * @param sender The command sender to add
     * @param message The message to send
     */
    void sendKey(CommandSender sender, String message);

    /**
     * Parses the given arguments and sends a message to all receivers plus the
     * given command sender.
     * <p />
     * This uses a key instead of a real message. If the message could not be found, a
     * default content will be generated.
     *
     * @param sender The command sender to add
     * @param message The message to send
     * @param args The arguments to parse
     */
    void sendKey(CommandSender sender, String message, Object... args);

}
