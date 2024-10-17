package net.respawn.slicegames.states;

import com.respawnnetwork.respawnlib.gameapi.states.WaitForPlayersState;
import net.respawn.slicegames.SliceGames;
import net.respawn.slicegames.modules.SliceGamesModule;

/**
 * Represents the wait countdown for the tributes.
 *
 * @author spaceemotion
 * @version 1.0
 */
public class WaitForTributesState extends WaitForPlayersState<SliceGames> {

    public WaitForTributesState(SliceGames sliceGames) {
        super(sliceGames);
    }

    @Override
    protected int getAmountNeeded() {
        SliceGamesModule module = getGame().getModule(SliceGamesModule.class);
        return module != null ? module.getPodiums().size() : 12;
    }

    @Override
    public String getDisplayName() {
        return "Wait for Tributes";
    }

    @Override
    public String getName() {
        return "wait-for-tributes";
    }

}
