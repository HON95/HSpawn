package no.hon95.bukkit.hspawn;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


public final class HSpawnPlugin extends JavaPlugin {

	private final ConfigManager gConfigManager = new ConfigManager(this);
	private final HCommandExecutor gCommandExecutor = new HCommandExecutor(this);
	private final PlayerListener gPlayerListener = new PlayerListener(this);

	private boolean gEnable = true;
	private boolean gSafeY = true;
	private Permission gPermissionPlugin = null;

	@Override
	public void onLoad() {
		gConfigManager.load();
	}

	@Override
	public void onEnable() {
		if (!gEnable) {
			getLogger().warning("Plugin disabled by config.");
			getPluginLoader().disablePlugin(this);
			return;
		}
		gConfigManager.reload();
		hookIntoVault();

		getServer().getPluginManager().registerEvents(gPlayerListener, this);
		getCommand("spawn").setExecutor(gCommandExecutor);
	}

	@Override
	public void onDisable() {
		gConfigManager.saveSpawns();
	}

	private boolean hookIntoVault() {
		try {
			Class.forName("net.milkbowl.vault.permission.Permission");
		} catch (ClassNotFoundException ex) {
			getLogger().warning("Vault not found!");
			return false;
		}
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServicesManager().getRegistration(Permission.class);
		gPermissionPlugin = rsp.getProvider();
		if (gPermissionPlugin == null) {
			getLogger().warning("Failed to hook into Vault!");
			return false;
		}
		return true;
	}

	public String getPlayerGroup(String world, String player) {
		if (gPermissionPlugin == null)
			return null;
		return gPermissionPlugin.getPrimaryGroup(world, player);
	}

	public void setEnable(boolean enable) {
		gEnable = enable;
	}

	public void setSafeY(boolean safeY) {
		gSafeY = safeY;
	}

	public boolean getSafeY() {
		return gSafeY;
	}

	public ConfigManager getConfigManager() {
		return gConfigManager;
	}

	public Location makeYSafe(Location location) {
		if (!gSafeY)
			return location;
		World world = location.getWorld();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();
		do {
			if (world.getBlockAt(x, y, z).getType() == Material.AIR && world.getBlockAt(x, y + 1, z).getType() == Material.AIR) {
				location.setY(y);
				return location;
			}
		} while (++y < 256);
		getLogger().warning("Failed to find safe Y location for spawn " + x + "," + y + "," + z + " in world " + world.getName());
		return location;
	}
}
