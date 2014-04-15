package no.hon95.bukkit.hspawn;

import java.util.HashMap;
import java.util.Map;


public final class HWorld {

	private final HashMap<String, HSpawn> gGroupSpawns = new HashMap<String, HSpawn>();

	public Map<String, HSpawn> getGroupSpawns() {
		return gGroupSpawns;
	}
}
