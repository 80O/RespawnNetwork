package im.xpd.parkour;

import lombok.AllArgsConstructor;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@AllArgsConstructor
public class CoursePotionEffect {
	private final PotionEffectType potionEffectType;
	private final int level;


	public PotionEffect getPotionEffect() {
        return potionEffectType.createEffect(20 * 60 * 60 * 24, level);
	}

}
