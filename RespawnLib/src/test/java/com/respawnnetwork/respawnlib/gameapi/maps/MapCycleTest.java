package com.respawnnetwork.respawnlib.gameapi.maps;

import org.bukkit.configuration.MemoryConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.LinkedList;
import java.util.List;

@RunWith(JUnit4.class)
public class MapCycleTest {

    @Test
    public void testCycle() throws Exception {
        List<GameMap> maps = new LinkedList<>();
        for (int i = 0; i < 3; i++) {
            maps.add(new GameMap("testmap-" + i, new MemoryConfiguration()));
        }

        MapCycle<GameMap> cycle = new MapCycle.Randomized<>(maps);

        Assert.assertNotNull("Next map is NULL!", cycle.getNext());
    }

}
