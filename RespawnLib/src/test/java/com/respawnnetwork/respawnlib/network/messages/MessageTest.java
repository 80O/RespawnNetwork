package com.respawnnetwork.respawnlib.network.messages;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RunWith(JUnit4.class)
public class MessageTest {

    @Test
    public void testMessages() throws Exception {
        List<CommandSender> receivers = new ArrayList<>();
        receivers.add(new DummySender("Bob"));
        receivers.add(new DummySender("Peter"));
        receivers.add(new DummySender("Justus"));

        Message.DANGER.receivers(receivers).send("Incoming missile!");
    }


    private static class DummySender implements CommandSender {
        private final String name;


        private DummySender(String name) {
            this.name = name;
        }

        @Override
        public void sendMessage(String s) {
            System.out.println("<" + name + "> " + s);
        }

        @Override
        public void sendMessage(String[] strings) {
            for (String line : strings) {
                sendMessage(line);
            }
        }

        @Override
        public Server getServer() {
            return null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isPermissionSet(String s) {
            return true;
        }

        @Override
        public boolean isPermissionSet(Permission permission) {
            return true;
        }

        @Override
        public boolean hasPermission(String s) {
            return true;
        }

        @Override
        public boolean hasPermission(Permission permission) {
            return true;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int i) {
            return null;
        }

        @Override
        public void removeAttachment(PermissionAttachment permissionAttachment) {

        }

        @Override
        public void recalculatePermissions() {

        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            return null;
        }

        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean b) {

        }
    }

}
