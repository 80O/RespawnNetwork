package com.respawnnetwork.respawnlobby.network;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.net.InetAddress;
import java.util.logging.Logger;

@RunWith(JUnit4.class)
public class PingTest {

    @Test
    public void pingTest() throws Exception {
        Ping server = new Ping(InetAddress.getByName("foxtrot.srv.respawnnetwork.com"), 7001, 7000);
        server.fetch(Logger.getGlobal());

        System.out.print("Status: " + server.getMotd() + "\nOnline: " + server.getOnline() + "\nMax: " + server.getMax() + "\n");
    }

}
