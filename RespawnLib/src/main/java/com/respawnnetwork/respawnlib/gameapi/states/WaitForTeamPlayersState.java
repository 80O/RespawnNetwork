package com.respawnnetwork.respawnlib.gameapi.states;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;

/**
 * @author spaceemotion
 * @since 1.0.1
 */
public class WaitForTeamPlayersState<G extends Game> extends WaitForPlayersState<G> {

    public WaitForTeamPlayersState(G game) {
        super(game);
    }

    @Override
    protected int getAmountNeeded() {
        int amount = 0;
        GameModule module = getGame().getModule(TeamModule.class);

        if (module != null && module.isLoaded() && module instanceof TeamModule) {
            // Count and add up min. players
            for (Object team : ((TeamModule) module).getTeams()) {
                // Urrrgh...
                if (!(team instanceof Team)) {
                    continue;
                }

                amount += ((Team) team).getMinPlayers();
            }
        } else {
            amount = 2;
        }

        return amount;
    }

}
