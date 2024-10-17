package net.respawn.goldrush.modules.shop.types;

import com.respawnnetwork.respawnlib.gameapi.GamePlayer;
import com.respawnnetwork.respawnlib.gameapi.modules.team.Team;
import com.respawnnetwork.respawnlib.gameapi.modules.team.TeamModule;
import com.respawnnetwork.respawnlib.lang.Displayable;
import com.respawnnetwork.respawnlib.lang.ParseException;
import com.respawnnetwork.respawnlib.math.MersenneTwisterFast;
import com.respawnnetwork.respawnlib.network.messages.MessageCreator;
import gnu.trove.map.hash.THashMap;
import net.respawn.goldrush.GoldRush;
import net.respawn.goldrush.modules.shop.ShopItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Represents an effect with a target
 *
 * @author spaceemotion
 * @author olivervscreeper
 * @version 1.0
 */
public class EffectShopType implements ShopType {
    /** The effect target */
    public enum Target implements Displayable {
        RANDOM_OPPONENT("Random opponent"),
        RANDOM_YOUR_TEAM("Random team member"),
        PLAYER("you"),
        YOUR_TEAM("Your team"),
        OPPONENT_TEAM("Opponent team");

        public static final Map<String, Target> BY_NAME = new THashMap<>();
        static {
            for (Target target : values()) {
                BY_NAME.put(target.name(), target);
            }
        }

        private final String displayName;

        Target(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        public static Target byName(String name) {
            return name != null ? BY_NAME.get(name.toUpperCase().replace(' ', '_')) : null;
        }
    }


    private static final MersenneTwisterFast RANDOM = new MersenneTwisterFast();

    private final TeamModule<GoldRush> teamModule;


    public EffectShopType(TeamModule<GoldRush> teamModule){
        this.teamModule = teamModule;
    }

    @Override
    public String getName() {
        return "effect";
    }

    @Override
    public EffectShopItem parseConfig(Logger logger, int price, ConfigurationSection effectConfig) throws ParseException {
        // Get effect target
        String targetName = effectConfig.getString("target");
        if (targetName == null) {
            throw new ParseException("No target name specified!");
        }

        Target target = Target.byName(targetName);
        if (target == null) {
            throw new ParseException("Unknown effect target: " + targetName);
        }

        String name = effectConfig.getString("name", "Potion effect");

        // Build types
        List<PotionEffect> effects = new LinkedList<>();
        List<Map<?, ?>> fxMap = effectConfig.getMapList("effects");

        if (fxMap != null && !fxMap.isEmpty()) {
            for (Map<?, ?> map : fxMap) {
                effects.add(getPotionEffectType(new MemoryConfiguration().createSection("tmp", map)));
            }

        } else {
            // Get single effect type
            effects.add(getPotionEffectType(effectConfig));
        }

        return new EffectShopItem(price, name, effectConfig.getString("message"), target, effects);
    }

    private PotionEffect getPotionEffectType(ConfigurationSection section) throws ParseException {
        // Get effect type name
        String typeName = section.getString("effect");
        if (typeName == null) {
            throw new ParseException("Did not set potion effect!");
        }

        PotionEffectType potionEffectType = PotionEffectType.getByName(typeName.toUpperCase().replace(' ', '_'));
        if (potionEffectType == null) {
            throw new ParseException("Unknown potion effect type: " + typeName);
        }

        // Duration
        int duration = section.getInt("duration");
        if (duration == 0) {
            duration = Integer.MAX_VALUE;

        } else {
            duration *= 20;
        }

        // Amplifier
        int amplifier = section.getInt("amplifier");

        return new PotionEffect(potionEffectType, duration, amplifier, false);
    }


    private class EffectShopItem extends ShopItem {
        private final String name, message;
        private final EffectShopType.Target target;
        private final List<PotionEffect> effects;


        public EffectShopItem(int price, String name, String message, EffectShopType.Target target, List<PotionEffect> effects) {
            super(price);

            this.name = name;
            this.message = message;
            this.target = target;
            this.effects = effects;
        }

        @Override
        public boolean onBuyItem(GamePlayer gamePlayer) {
            Player player = gamePlayer.getPlayer();
            if (player == null) {
                return false;
            }

            MessageCreator msg = gamePlayer.getGame().createMessage().create();
            boolean all = true;

            switch (target) {
                case PLAYER:
                    // Give the player the potion effect
                    addPotionEffects(player, effects);
                    all = false;
                    break;

                case YOUR_TEAM:
                    // Give the whole team an effect
                    Team playerTeam = teamModule.getTeam(gamePlayer);

                    if (playerTeam != null) {
                        for(GamePlayer gPl : playerTeam.getPlayers()){
                            Player teamPlayer = gPl.getPlayer();
                            if (teamPlayer == null) {
                                continue;
                            }

                            addPotionEffects(teamPlayer, effects);
                        }

                        msg.provide("team", getDisplayName(playerTeam));
                    }

                    break;

                case OPPONENT_TEAM:
                    // Give the whole team an effect
                    Team yourTeam = null;

                    for(Team team : teamModule.getTeams()){
                        if (team.getPlayers().contains(gamePlayer)) {
                            yourTeam = team;
                            continue;
                        }

                        msg.provide("opponent", getDisplayName(team));

                        for(GamePlayer gPl : team.getPlayers()){
                            Player teamPlayer = gPl.getPlayer();
                            if (teamPlayer == null) {
                                continue;
                            }

                            addPotionEffects(teamPlayer, effects);
                        }
                    }

                    msg.provide("team", getDisplayName(yourTeam));
                    break;

                case RANDOM_OPPONENT:
                    // Give a random opponent the effect
                    for(Team team : teamModule.getTeams()){
                        if (team.getPlayers().contains(gamePlayer)) {
                            continue;
                        }

                        msg.provide("player", getDisplayName(applyToRandomTeamMember(team), team));
                    }
                    break;

                case RANDOM_YOUR_TEAM:
                    // Give a random team member the effect
                    for(Team team : teamModule.getTeams()){
                        if (!team.getPlayers().contains(gamePlayer)) {
                            continue;
                        }

                        msg.provide("player", getDisplayName(applyToRandomTeamMember(team), team));
                    }
            }

            if (message != null) {
                if (all) {
                    msg.send(message);

                } else {
                    msg.send(gamePlayer.getPlayer(), message);
                }
            }

            return true;
        }

        @Override
        public String getDisplayName() {
            return name + " for " + target.getDisplayName();
        }

        private void addPotionEffects(Player player, Collection<PotionEffect> potionEffects) {
            for (PotionEffect potionEffect : potionEffects) {
                player.addPotionEffect(potionEffect, true);
            }
        }

        private GamePlayer applyToRandomTeamMember(Team team) {
            if (team.getPlayers().isEmpty()) {
                return null;
            }

            GamePlayer victim = team.getPlayers().get(RANDOM.nextInt(team.getPlayers().size()));
            Player victimPlayer = victim != null ? victim.getPlayer() : null;

            if (victimPlayer != null) {
                addPotionEffects(victimPlayer, effects);
            }

            return victim;
        }

        private String getDisplayName(GamePlayer player, Team team) {
            if (player == null || team == null) {
                return "<null>";
            }

            return team.getTeam().getPrefix() + player.getName() + team.getTeam().getSuffix();
        }

        private String getDisplayName(Team team) {
            if (team == null) {
                return "<null>";
            }

            return team.getTeam().getPrefix() + team.getDisplayName() + team.getTeam().getSuffix();
        }

    }

}
