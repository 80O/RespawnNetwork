package net.respawn.slicegames.states;

import com.respawnnetwork.respawnlib.bukkit.Location;
import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import net.respawn.slicegames.SGPlayer;
import net.respawn.slicegames.SliceGames;
import net.respawn.slicegames.modules.SliceGamesModule;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Represents a prepare state for Slice Games.
 * <p />
 * Teleports the tributes to a random podium and refills all chests.
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0
 */
public class PrepareGameState extends CountdownState<SliceGames> {

    public PrepareGameState(SliceGames game) {
        super(game);
    }

    @Override
    protected void onEnter() {
        SliceGamesModule module = getGame().getModule(SliceGamesModule.class);

        if (module == null) {
            getLogger().info("No podium locations found, will stop game!");
            getGame().stopGame();
            return;
        }

        List<SGPlayer> players = Arrays.asList(getGame().getPlayers());
        Collections.shuffle(players);

        // Teleport tributes to random podiums
        int i = 0;

        List<Location> podiums = module.getPodiums();
        for(Location podium : podiums) {
            if (i >= podiums.size() || i >= players.size()) {
                continue;
            }

            SGPlayer player = players.get(i);

            player.teleportTo(podium);
            player.setSpectator(false);
            player.reset();
            player.giveCompass();

            // Set survival
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer != null) {
                bukkitPlayer.setGameMode(GameMode.SURVIVAL);
            }

            i++;
        }

        // Refill all chests
        module.refillInventories(false);

        super.onEnter();
    }

}
