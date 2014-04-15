package no.hon95.bukkit.hspawn;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;


public final class PlayerListener implements Listener {

	private final HSpawnPlugin gPlugin;

	public PlayerListener(HSpawnPlugin plugin) {
		gPlugin = plugin;
	}

	@EventHandler
	public void onEvent(PlayerRespawnEvent ev) {
		HSpawn spawn = gPlugin.getConfigManager().getSpawn(ev.getPlayer());
		if (ev.isBedSpawn() && spawn.getBedRespawn())
			return;
		ev.setRespawnLocation(gPlugin.makeYSafe(spawn.toLocation()));
	}

	@EventHandler
	public void onEvent(PlayerJoinEvent ev) {
		HSpawn spawn = gPlugin.getConfigManager().getSpawn(ev.getPlayer());
		if (ev.getPlayer().hasPlayedBefore() && !spawn.getTpToSpawnOnJoin())
			return;
		ev.getPlayer().teleport(gPlugin.makeYSafe(spawn.toLocation()));
	}

	@EventHandler
	public void onEvent(WorldLoadEvent ev) {
		gPlugin.getConfigManager().loadSpawnsForWorld(ev.getWorld());
	}

	@EventHandler
	public void onEvent(WorldUnloadEvent ev) {
		gPlugin.getConfigManager().unloadSpawnsForWorld(ev.getWorld());
	}
}
