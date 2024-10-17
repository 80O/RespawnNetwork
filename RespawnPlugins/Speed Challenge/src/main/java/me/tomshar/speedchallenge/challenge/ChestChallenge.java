package me.tomshar.speedchallenge.challenge;

import me.tomshar.speedchallenge.Shrine;
import me.tomshar.speedchallenge.util.Challenges;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tom on 10/03/14.
 */
public class ChestChallenge extends Challenge {

	private Shrine shrine;
	private List<ItemStack> items = new ArrayList<>();
	private List<ItemStack> collected = new ArrayList<>();

	public ChestChallenge(Challenges challenge, Shrine shrine, List<ItemStack> items) {
		super(challenge);
		this.shrine = shrine;
		this.items = items;
	}

	@Override
	public boolean checkWinCondition() {
		boolean completed = getRemaining().isEmpty();

		if(completed != isCompleted()) {
			setCompleted(completed);
			return isCompleted();
		}

		return false;
	}

	public List<ItemStack> getItems() { return items; }

	public List<ItemStack> getCollected() {
		collected.clear();

		for(ItemStack item : shrine.getChest().getBlockInventory().getContents()) {
			if(item == null) break;

			collected.add(item);
		}

		return collected;
	}

	public List<ItemStack> getRemaining() {
		List<ItemStack> remaining = items;
		remaining.removeAll(getCollected());

		System.out.println(remaining);

		return remaining;
	}

}
