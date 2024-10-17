package net.respawn.havok.util;

import net.respawn.havok.Havok;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Tom on 19/03/14.
 */
public class BungeeUtils {

	public static void returnPlayer(String server, Player p) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		DataOutputStream dout = new DataOutputStream(bout);
		try {
			dout.writeUTF("Connect");
			dout.writeUTF(server);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		p.sendPluginMessage(Havok.instance, "BungeeCord", bout.toByteArray());

		try {
			bout.close();
			dout.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}
