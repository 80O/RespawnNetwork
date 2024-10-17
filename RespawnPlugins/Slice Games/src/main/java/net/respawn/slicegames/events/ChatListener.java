package net.respawn.slicegames.events;

import lombok.RequiredArgsConstructor;
import net.respawn.slicegames.SGPlayer;
import net.respawn.slicegames.SliceGames;
import net.respawn.slicegames.SliceGamesPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Represents a listener for chat messages.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
@RequiredArgsConstructor
public class ChatListener implements Listener {
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("[mm:ss]");

    private final SliceGamesPlugin plugin;


    @EventHandler
    public void onMessage(AsyncPlayerChatEvent event) {
        SliceGames game = plugin.getCurrentGame();
        Date date = new Date(0);

        if (game != null) {
            // Remove real players from the recipients if the message got sent by a spectator
            if (game.isSpectator(event.getPlayer())) {
                for(SGPlayer player : game.getRealPlayers()) {
                    event.getRecipients().remove(player.getPlayer());
                }
            }

            // Get elapsed time from the map
            date = game.getMap().getElapsedTime();
        }

        // Format message to include the elapsed time
        event.setFormat("§3" + TIME_FORMAT.format(date) + " §6%s§r: %s");
    }

}
