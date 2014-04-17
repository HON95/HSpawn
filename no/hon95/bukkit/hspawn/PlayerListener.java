package no.hon95.bukkit.hspawn;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;


public final class PlayerListener implements Listener {

	private final HSpawnPlugin gPlugin;

	public PlayerListener(HSpawnPlugin plugin) {
		gPlugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onEvent(PlayerRespawnEvent ev) {
		HSpawn spawn = gPlugin.getConfigManager().getSpawn(ev.getPlayer());
		if (ev.isBedSpawn() && spawn.getBedRespawn())
			return;
		Location location = spawn.toLocation();
		if (location.getWorld() == null) {
			gPlugin.getLogger().warning("World " + spawn.getSpawnWorld() + " is not found.");
			return;
		}
		location = gPlugin.makeYSafe(location);
		ev.setRespawnLocation(location);
		playDelayedEffect(location);
	}

	@EventHandler
	public void onEvent(PlayerJoinEvent ev) {
		HSpawn spawn = gPlugin.getConfigManager().getSpawn(ev.getPlayer());
		if (ev.getPlayer().hasPlayedBefore() && !spawn.getTpToSpawnOnJoin())
			return;
		Location location = spawn.toLocation();
		if (location.getWorld() == null) {
			gPlugin.getLogger().warning("World " + spawn.getSpawnWorld() + " is not found.");
			return;
		}
		location = gPlugin.makeYSafe(location);
		ev.getPlayer().teleport(location, TeleportCause.PLUGIN);
		playDelayedEffect(location);
	}

	@EventHandler
	public void onEvent(WorldLoadEvent ev) {
		gPlugin.getConfigManager().loadSpawnsForWorld(ev.getWorld());
	}

	@EventHandler
	public void onEvent(WorldUnloadEvent ev) {
		gPlugin.getConfigManager().unloadSpawnsForWorld(ev.getWorld());
	}

	private void playDelayedEffect(final Location location) {
		gPlugin.getServer().getScheduler().runTaskLater(gPlugin, new Runnable() {
			public void run() {
				location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
			}
		}, 1L);
	}
}
