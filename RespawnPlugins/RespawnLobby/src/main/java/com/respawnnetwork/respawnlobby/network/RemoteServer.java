package com.respawnnetwork.respawnlobby.network;

import lombok.Getter;

import java.net.InetAddress;

/**
 * Represents a connection to a remote server using the IP and port.
 *
 * @author spaceemotion
 * @version 1.0
 */
@Getter
public class RemoteServer {
    /** The server address */
    private final InetAddress address;

    /** The server port */
    private final int port;


    /**
     * Creates a new remote server object.
     *
     * @param address The server address we're connecting to
     * @param port The remote server port
     */
    public RemoteServer(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

}
