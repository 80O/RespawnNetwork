package com.respawnnetwork.respawnlib.lang;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Represents an object that can be received by a number of command senders.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public interface Receivable<R extends Receivable> {

    /**
     * Adds an array of command senders to the list of people that get the object.
     *
     * @param array The array of command senders
     * @return A receivable instance for method chaining
     */
    R receivers(CommandSender... array);

    /**
     * Adds a list of command senders to the list of people that get the object.
     *
     * @param list The list of command senders
     * @return A receivable instance for method chaining
     */
    R receivers(List<CommandSender> list);

}
