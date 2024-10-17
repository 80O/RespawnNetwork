package net.respawn.pointrunner.states;

import com.respawnnetwork.respawnlib.gameapi.states.WaitForPlayersState;
import net.respawn.pointrunner.PointRunner;
import net.respawn.pointrunner.modules.PointRunnerModule;


public class WaitForPRPlayersState extends WaitForPlayersState<PointRunner> {

    public WaitForPRPlayersState(PointRunner game) {
        super(game);
    }

    @Override
    protected int getAmountNeeded() {
        PointRunnerModule module = getGame().getModule(PointRunnerModule.class);
        return getGame().getConfig().getInt(module == null ? "point-runner" : module.getName() + ".playersNeeded", 2);
    }

}
