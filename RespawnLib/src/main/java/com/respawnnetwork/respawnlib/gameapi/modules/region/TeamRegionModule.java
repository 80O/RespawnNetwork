package com.respawnnetwork.respawnlib.gameapi.modules.region;

import com.respawnnetwork.respawnlib.gameapi.Game;
import com.respawnnetwork.respawnlib.gameapi.GameModule;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import lombok.Getter;
import org.bukkit.World;

/**
 * Represents a team based region module.
 *
 * @author spaceemotion
 * @version 1.0
 * @since 1.0.1
 */
public class TeamRegionModule<G extends Game> extends RegionModule<G> {
    @Getter
    private TeamModule teamModule;


    public TeamRegionModule(G game) {
        super(game);
    }

    public TeamRegionModule(G game, int factor) {
        super(game, factor);
    }

    @Override
    protected boolean onEnable() {
        GameModule module = getGame().getModule(TeamModule.class);

        if (module != null && module.isLoaded() && module instanceof TeamModule) {
            teamModule = (TeamModule) module;

        } else {
            getLogger().warning("Team module hasn't been loaded yet, Team regions won't work!");
        }

        return super.onEnable();
    }

    @Override
    protected Region createRegion(String name, World world, Vector3i min, Vector3i max) {
        if (teamModule != null) {
            Team team = teamModule.getTeam(name);

            if (team != null) {
                return createRegion(team, world, min, max);
            }
        }

        return super.createRegion(name, world, min, max);
    }

    /**
     * Creates a new team region instance.
     *
     * @param team  The team associated with the region
     * @param world The world the region is in
     * @param min   The minimum point / location
     * @param max   The maximum point / location
     * @return The created team region
     */
    protected TeamRegion createRegion(Team team, World world, Vector3i min, Vector3i max) {
        return new TeamRegion(team, world, min, max);
    }


    /**
     * Represents a region that is associated with a {@link com.respawnnetwork.respawnlib.gameapi.modules.team.Team}.
     *
     * @author spaceemotion
     * @version 1.0
     * @since 1.0.1
     */
    public static class TeamRegion extends Region.Default {
        @Getter
        private final Team team;


        public TeamRegion(Team team, World world, Vector3i first, Vector3i second) {
            super(team.getName(), world, first, second);

            this.team = team;
        }
    }

}
