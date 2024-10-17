package com.respawnnetwork.respawnlib.network.signs;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.material.MaterialData;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.plugin.Plugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RunWith(JUnit4.class)
public class SignTest {

    @Test
    public void signTest() {
        MySign sign = new MySign("test");
        new SignBuilder(sign).provide("key", true).provide("key2", 1000)
                .lines("line 1", "Line {key2}", "line 3 {key}", "this line is too long for a sign")
                .apply();

        System.out.print(Arrays.toString(sign.getLines()));
    }

    public static class MySign implements Sign {

        private Map<Integer, String> lines = new LinkedHashMap<>();

        public MySign(String test) {
            lines.put(0, test);
        }

        @Override
        public String[] getLines() {
            return lines.values().toArray(new String[lines.size()]);
        }

        @Override
        public String getLine(int i) throws IndexOutOfBoundsException {
            return lines.get(i);
        }

        @Override
        public void setLine(int i, String s) throws IndexOutOfBoundsException {
            lines.put(i, s);
        }

        @Override
        public Block getBlock() {
            return null;
        }

        @Override
        public MaterialData getData() {
            return null;
        }

        @Override
        public Material getType() {
            return null;
        }

        @Override
        public int getTypeId() {
            return 0;
        }

        @Override
        public byte getLightLevel() {
            return 0;
        }

        @Override
        public World getWorld() {
            return null;
        }

        @Override
        public int getX() {
            return 0;
        }

        @Override
        public int getY() {
            return 0;
        }

        @Override
        public int getZ() {
            return 0;
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Location getLocation(Location location) {
            return null;
        }

        @Override
        public Chunk getChunk() {
            return null;
        }

        @Override
        public void setData(MaterialData materialData) {

        }

        @Override
        public void setType(Material material) {

        }

        @Override
        public boolean setTypeId(int i) {
            return false;
        }

        @Override
        public boolean update() {
            return false;
        }

        @Override
        public boolean update(boolean b) {
            return false;
        }

        @Override
        public boolean update(boolean b, boolean b2) {
            return false;
        }

        @Override
        public byte getRawData() {
            return 0;
        }

        @Override
        public void setRawData(byte b) {

        }

        @Override
        public void setMetadata(String s, MetadataValue metadataValue) {

        }

        @Override
        public List<MetadataValue> getMetadata(String s) {
            return null;
        }

        @Override
        public boolean hasMetadata(String s) {
            return false;
        }

        @Override
        public void removeMetadata(String s, Plugin plugin) {

        }
    }

}
