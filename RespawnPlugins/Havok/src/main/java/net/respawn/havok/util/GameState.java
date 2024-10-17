package net.respawn.havok.util;

/**
 * Created by Tom on 19/03/14.
 */
public enum GameState {
	PRE_GAME("Waiting"), IN_GAME("In Game"), END_GAME("Reloading");

	private final String status;

	private GameState(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

}
