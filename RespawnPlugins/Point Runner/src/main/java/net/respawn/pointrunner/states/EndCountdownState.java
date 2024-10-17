package net.respawn.pointrunner.states;

import com.respawnnetwork.respawnlib.gameapi.states.CountdownState;
import com.respawnnetwork.respawnlib.gameapi.states.InGameState;
import net.respawn.pointrunner.PRPlayer;
import net.respawn.pointrunner.PointRunner;
import org.bukkit.Sound;


public class EndCountdownState extends CountdownState<PointRunner> implements InGameState {

    public EndCountdownState(PointRunner game) {
        super(game);
    }

    @Override
    protected void onEnter() {
        if (getGame().getNumberOfPlayers() <= 1) {
            getGame().nextState();
            return;
        }

        super.onEnter();
    }

    @Override
    public void onLeave() {
        super.onLeave();

        int maxPoints = 0;
        PRPlayer winner = null;

        for (PRPlayer player : getGame().getPlayers()) {
            int points = (int) player.getStatistics().get(PointRunner.POINTS);

            if (points > maxPoints) {
                maxPoints = points;
                winner = player;
            }
        }

        // Announce winner
        if (winner != null) {
            getGame().getMap().setWinner(winner);

            getGame().createMessage().provide("winner", winner.getName())
                    .provide("points", maxPoints)
                    .provide("pointStr", maxPoints == 1 ? "point" : "points").sendKey("game.win");
        }
    }

    @Override
    protected Sound getCountdownSound() {
        return Sound.NOTE_BASS_GUITAR;
    }

    @Override
    public String getDisplayName() {
        return "End Countdown";
    }

    @Override
    public String getName() {
        return "end-countdown";
    }

}
