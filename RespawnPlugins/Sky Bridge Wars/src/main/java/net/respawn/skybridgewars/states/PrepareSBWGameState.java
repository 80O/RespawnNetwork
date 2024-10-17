package net.respawn.skybridgewars.states;

import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import net.respawn.skybridgewars.SkyBridgeWars;


public class PrepareSBWGameState extends CountdownState<SkyBridgeWars> {

    public PrepareSBWGameState(SkyBridgeWars game) {
        super(game);
    }

    @Override
    public void onEnter() {
        // Assign players to teams
        TeamModule module = getGame().getModule(TeamModule.class);
        if (module != null && module.isLoaded()) {
            module.assignTeamsToPlayers();
        }

        super.onEnter();
    }

}
