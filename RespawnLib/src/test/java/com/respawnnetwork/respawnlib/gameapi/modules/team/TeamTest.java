package com.respawnnetwork.respawnlib.gameapi.modules.team;

import org.junit.Assert;
import org.junit.Test;


public class TeamTest {

    @Test
    public void testColors() throws Exception {
        Assert.assertEquals("Wrong color assignment", 0xFF5555, TeamColor.RED.getColor().asRGB());
    }

}
