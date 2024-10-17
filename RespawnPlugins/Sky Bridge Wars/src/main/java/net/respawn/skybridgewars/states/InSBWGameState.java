package net.respawn.skybridgewars.states;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.gameapi.states.DefaultInGameState;
import net.respawn.skybridgewars.SkyBridgeWars;
import net.respawn.skybridgewars.modules.SBWMechanicsModule;


public class InSBWGameState extends DefaultInGameState<SkyBridgeWars> {


    public InSBWGameState(SkyBridgeWars game) {
        super(game);
    }

    @Override
    protected void onEnter() {
        SBWMechanicsModule mechanics = getGame().getModule(SBWMechanicsModule.class);

        if (mechanics == null || !mechanics.isLoaded()) {
            getLogger().warning("SBW Mechanics module does not exist or is not loaded!");

        } else {
            // Give players their start items
            for (GamePlayer player : getGame().getRealPlayers()) {
                mechanics.resetShopItems(player);
            }
        }

        super.onEnter();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onLeave() {
        TeamModule<SkyBridgeWars> teamModule = getGame().getModule(TeamModule.class);
        if (teamModule == null) {
            getLogger().warning("Could not get team module, will not be able to decide winner");
            return;
        }

        Team winner = null;
        int maxScore = 0;

        for (Team team : teamModule.getTeams()) {
            // Decide winner
            if (maxScore < team.getScore()) {
                maxScore = team.getScore();
                winner = team;
            }

            // Clear potion effects
            for (GamePlayer gamePlayer : team.getPlayers()) {
                gamePlayer.clearPotionEffects();
            }
        }

        if (winner != null) {
            getGame().createMessage().provide("winner", winner.getScoreboardName()).sendKey("sbw.win");
        }
    }

}
