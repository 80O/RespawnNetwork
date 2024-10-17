package net.respawn.goldrush.states;

import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import net.respawn.goldrush.GoldRush;


public class PrepareGameState extends CountdownState<GoldRush> {

    public PrepareGameState(GoldRush game) {
        super(game);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onEnter() {
        // Assign players to teams
        TeamModule<GoldRush> module = getGame().getModule(TeamModule.class);
        if (module != null && module.isLoaded()) {
            module.assignTeamsToPlayers();

            for (Team team : module.getTeams()) {
                team.setScore(0);
            }
        }

        super.onEnter();
    }

}
