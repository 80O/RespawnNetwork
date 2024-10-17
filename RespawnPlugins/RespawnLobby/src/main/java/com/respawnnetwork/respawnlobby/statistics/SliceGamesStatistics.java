package com.respawnnetwork.respawnlobby.statistics;

import com.respawnnetwork.respawnlobby.RespawnLobby;
import gnu.trove.map.TIntObjectMap;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Game statistics for slice games
 *
 * @author spaceemotion
 * @author TomShar
 * @version 1.0.1
 */
public class SliceGamesStatistics extends ServerStatistic {
    private final TIntObjectMap<StatisticsSign> top3;
    private final StatisticsSign melon;


    public SliceGamesStatistics(RespawnLobby plugin, TIntObjectMap<StatisticsSign> top3, StatisticsSign melon) {
        super(plugin);

        this.top3 = top3;
        this.melon = melon;

        new Update().runTaskTimer(plugin, 0, 10 * 60 * 20);
    }
/*
    public Result getTopThreeWinners() {
        Database db = getPlugin().getDatabaseManager();
        if (db == null) {
            return null;
        }

        db.open();

        DSLContext ctx = db.getContext();
        Result res = ctx.select(HG_USERS.UUID, HG_USERS.WINS).from(HG_USERS).orderBy(HG_USERS.WINS.desc()).limit(3).fetch();

        db.close();

        return res;
    }

    public int totalMelons() {
        Database db = getPlugin().getDatabaseManager();
        if (db == null) {
            return 0;
        }

        db.open();

        DSLContext ctx = db.getContext();
        int res = ctx.select(HG_USERS.MELONS_EATEN.sum()).from(HG_USERS).fetchOne().value1().intValueExact();

        db.close();

        return res;
    }
*/
    @Override
    public void fillInformation() {
        /*final Result res = getTopThreeWinners();

        top3.forEachEntry(new TIntObjectProcedure<StatisticsSign>() {
            @Override
            public boolean execute(int map, StatisticsSign statisticsSign) {
                String uuid = (String) res.getValue(map, HG_USERS.UUID);
                int wins = (Integer) res.getValue(map, HG_USERS.WINS);
                String name = MojangAccount.fromUUID(uuid).getName();

                Sign sign = statisticsSign.getSign();
                new SignBuilder(sign).lines(statisticsSign.getLines().toArray(new String[statisticsSign.getLines().size()]))
                        .provide("name", name).provide("wins", wins)
                        .apply();

                Skull skull = (Skull) sign.getLocation().add(1, 1, 0).getBlock().getState();
                skull.setOwner(name);
                skull.update(true);

                return true;
            }
        });

        SignBuilder melonSign = new SignBuilder(melon.getSign());
        melonSign.lines(melon.getLines().toArray(new String[melon.getLines().size()])).provide("meloncount", totalMelons()).apply();*/
    }


    private class Update extends BukkitRunnable {

        @Override
        public void run() {
            fillInformation();
        }

    }

}
