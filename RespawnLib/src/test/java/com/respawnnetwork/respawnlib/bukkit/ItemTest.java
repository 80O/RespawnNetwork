package com.respawnnetwork.respawnlib.bukkit;

import org.bukkit.Material;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ItemTest {

    @Test
    public void testItemNames() throws Exception {
        Assert.assertEquals("Name does not match", "Cobblestone", Item.getHumanReadableName(Material.COBBLESTONE));
        Assert.assertEquals("Name does not match", "Book and Quill", Item.getHumanReadableName(Material.BOOK_AND_QUILL));
        Assert.assertNotEquals("Names do match", "Carrot item", Item.getHumanReadableName(Material.CARROT_ITEM));
    }

}
