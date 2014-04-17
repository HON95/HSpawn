package no.hon95.bukkit.hspawn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;


public final class SpawnManager {

	private static final String DEFAULT_GROUP = "default";
	private static final String KEY_FORMAT = "%s.%s.%s";
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private static final String CONFIG_HEADER = "Configuration file for hSpawn.";
	private static final String SPAWNS_HEADER = "Spawn configuration file for hSpawn."
			+ LINE_SEPARATOR + "This file is grouped into worlds and then groups."
			+ LINE_SEPARATOR + "The group ID needs to be the same as in your permission plugin."
			+ LINE_SEPARATOR + "'default' is the default group and is used if none of the others match.";

	private final HSpawnPlugin gPlugin;
	private final File gConfigFile;
	private final File gSpawnsFile;
	private YamlConfiguration gSpawnsConf;
	private HashMap<String, HWorld> gWorlds = new HashMap<String, HWorld>();
	boolean gChange = false;

	public SpawnManager(HSpawnPlugin plugin) {
		gPlugin = plugin;
		gConfigFile = new File(gPlugin.getDataFolder(), "config.yml");
		gSpawnsFile = new File(gPlugin.getDataFolder(), "spawns.yml");
	}

	public void load() {
		loadConfig();
		loadSpawnsConfig();
	}

	private void loadConfig() {
		boolean change = false;
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(gConfigFile);
		conf.options().copyHeader(true);
		conf.options().header(CONFIG_HEADER);

		if (!gConfigFile.isFile())
			change = true;
		if (!conf.isBoolean("enable")) {
			conf.set("enable", true);
			change = true;
		}
		gPlugin.setEnable(conf.getBoolean("enable"));
		if (!conf.isBoolean("safe_y")) {
			conf.set("safe_y", true);
			change = true;
		}
		gPlugin.setSafeY(conf.getBoolean("safe_y"));
		if (!conf.isBoolean("check_for_updates")) {
			conf.set("check_for_updates", true);
			change = true;
		}
		gPlugin.setCheckForUpdates(conf.getBoolean("check_for_updates"));

		if (change)
			saveFile(conf, gConfigFile);
	}

	private void loadSpawnsConfig() {
		gSpawnsConf = YamlConfiguration.loadConfiguration(gSpawnsFile);
		gSpawnsConf.options().copyHeader(true);
		gSpawnsConf.options().header(SPAWNS_HEADER);
		if (!gSpawnsFile.isFile()) {
			gChange = true;
			saveSpawns();
		}
	}

	public void reload() {
		loadConfig();
		loadSpawnsConfig();
		gWorlds.clear();
		for (World world : Bukkit.getWorlds())
			loadSpawnsForWorld(world, false);
		saveSpawns();
	}

	public void loadSpawnsForWorld(World world) {
		loadSpawnsForWorld(world, true);
	}

	public void loadSpawnsForWorld(World world, boolean save) {
		if (!gSpawnsConf.isConfigurationSection(world.getName())) {
			gSpawnsConf.set(world.getName(), null);
			gChange = true;
		}
		HWorld hworld = new HWorld();
		Location vanillaSpawn = world.getSpawnLocation();
		HSpawn defSpawn = loadDefaultSpawn(world.getName(), vanillaSpawn);
		hworld.getGroupSpawns().put(DEFAULT_GROUP, defSpawn);
		for (String group : gSpawnsConf.getConfigurationSection(world.getName()).getKeys(false)) {
			if (group.equalsIgnoreCase(DEFAULT_GROUP))
				continue;
			HSpawn spawn = loadSpawn(world.getName(), group, defSpawn);
			hworld.getGroupSpawns().put(group, spawn);
		}
		gWorlds.put(world.getName(), hworld);
		if (save)
			saveSpawns();
	}

	public void unloadSpawnsForWorld(World world) {
		gWorlds.remove(world.getName());
	}

	private HSpawn loadDefaultSpawn(String world, Location vanillaSpawn) {
		String group = DEFAULT_GROUP;
		String keyX = String.format(KEY_FORMAT, world, group, "x");
		String keyY = String.format(KEY_FORMAT, world, group, "y");
		String keyZ = String.format(KEY_FORMAT, world, group, "z");
		String keyPitch = String.format(KEY_FORMAT, world, group, "pitch");
		String keyYaw = String.format(KEY_FORMAT, world, group, "yaw");
		String keySpawnWorld = String.format(KEY_FORMAT, world, group, "spawn_world");
		String keyBedRespawn = String.format(KEY_FORMAT, world, group, "bed_respawn");
		String keyTpToSpawnOnJoin = String.format(KEY_FORMAT, world, group, "tp_to_spawn_on_join");

		if (!gSpawnsConf.isDouble(keyX)) {
			gSpawnsConf.set(keyX, vanillaSpawn.getX());
			gChange = true;
		}
		if (!gSpawnsConf.isDouble(keyY)) {
			gSpawnsConf.set(keyY, vanillaSpawn.getY());
			gChange = true;
		}
		if (!gSpawnsConf.isDouble(keyZ)) {
			gSpawnsConf.set(keyZ, vanillaSpawn.getZ());
			gChange = true;
		}
		if (!gSpawnsConf.isDouble(keyPitch)) {
			gSpawnsConf.set(keyPitch, (double) vanillaSpawn.getPitch());
			gChange = true;
		}
		if (!gSpawnsConf.isDouble(keyYaw)) {
			gSpawnsConf.set(keyYaw, (double) vanillaSpawn.getYaw());
			gChange = true;
		}
		if (!gSpawnsConf.isString(keySpawnWorld)) {
			gSpawnsConf.set(keySpawnWorld, getAssumedSpawnWorld(world));
			gChange = true;
		}
		if (!gSpawnsConf.isBoolean(keyBedRespawn)) {
			gSpawnsConf.set(keyBedRespawn, true);
			gChange = true;
		}
		if (!gSpawnsConf.isBoolean(keyTpToSpawnOnJoin)) {
			gSpawnsConf.set(keyTpToSpawnOnJoin, false);
			gChange = true;
		}

		return loadSpawn(world, group, null);
	}

	private HSpawn loadSpawn(String world, String group, HSpawn defSpawn) {
		String keyX = String.format(KEY_FORMAT, world, group, "x");
		String keyY = String.format(KEY_FORMAT, world, group, "y");
		String keyZ = String.format(KEY_FORMAT, world, group, "z");
		String keyPitch = String.format(KEY_FORMAT, world, group, "pitch");
		String keyYaw = String.format(KEY_FORMAT, world, group, "yaw");
		String keySpawnWorld = String.format(KEY_FORMAT, world, group, "spawn_world");
		String keyBedRespawn = String.format(KEY_FORMAT, world, group, "bed_respawn");
		String keyTpToSpawnOnJoin = String.format(KEY_FORMAT, world, group, "tp_to_spawn_on_join");
		String valSpawnWorld;
		double valX, valY, valZ;
		float valPitch, valYaw;
		boolean valBedRespawn, valTpToSpawnOnJoin;

		if (gSpawnsConf.isDouble(keyX))
			valX = gSpawnsConf.getDouble(keyX);
		else
			valX = defSpawn.getX();
		if (gSpawnsConf.isDouble(keyY))
			valY = gSpawnsConf.getDouble(keyY);
		else
			valY = defSpawn.getY();
		if (gSpawnsConf.isDouble(keyZ))
			valZ = gSpawnsConf.getDouble(keyZ);
		else
			valZ = defSpawn.getZ();
		if (gSpawnsConf.isDouble(keyPitch))
			valPitch = (float) gSpawnsConf.getDouble(keyPitch);
		else
			valPitch = defSpawn.getPitch();
		if (gSpawnsConf.isDouble(keyYaw))
			valYaw = (float) gSpawnsConf.getDouble(keyYaw);
		else
			valYaw = defSpawn.getYaw();
		if (gSpawnsConf.isString(keySpawnWorld))
			valSpawnWorld = gSpawnsConf.getString(keySpawnWorld);
		else
			valSpawnWorld = defSpawn.getSpawnWorld();
		if (gSpawnsConf.isBoolean(keyBedRespawn))
			valBedRespawn = gSpawnsConf.getBoolean(keyBedRespawn);
		else
			valBedRespawn = defSpawn.getBedRespawn();
		if (gSpawnsConf.isBoolean(keyTpToSpawnOnJoin))
			valTpToSpawnOnJoin = gSpawnsConf.getBoolean(keyTpToSpawnOnJoin);
		else
			valTpToSpawnOnJoin = defSpawn.getTpToSpawnOnJoin();

		return new HSpawn(valX, valY, valZ, valPitch, valYaw, valSpawnWorld, world, valBedRespawn, valTpToSpawnOnJoin);
	}

	public HSpawn getSpawn(Player player) {
		String playerName = player.getName();
		String world = player.getWorld().getName();
		String group = gPlugin.getPlayerGroup(world, playerName);
		if (group == null)
			group = DEFAULT_GROUP;

		ArrayList<String> worldsFound = new ArrayList<String>();
		HSpawn originalSpawn = getSpawnNonRedirected(group, world);
		HSpawn spawn = originalSpawn;
		while (!spawn.getSpawnWorld().equalsIgnoreCase(spawn.getFromWorld())) {
			if (worldsFound.contains(spawn.getFromWorld())) {
				gPlugin.getLogger().warning("Recursive spawns found for group " + group + " in world " + world + ".");
				gPlugin.getLogger().warning("Please make sure specified spawn_worlds don't loop back.");
				gPlugin.getLogger().warning(player.getName() + " will spawn in the same world as he/she already is.");
				player.sendMessage(ChatColor.RED + "You will spawn in the same world, because of recursive spawns.");
				return originalSpawn;
			}
			worldsFound.add(spawn.getFromWorld());
			world = spawn.getSpawnWorld();
			group = gPlugin.getPlayerGroup(world, playerName);
			spawn = getSpawnNonRedirected(group, world);
		}
		return spawn;
	}

	private HSpawn getSpawnNonRedirected(String group, String worldName) {
		HWorld world = gWorlds.get(worldName);
		if (world == null) {
			gPlugin.getLogger().warning("[BUG] World not added: " + worldName);
			return null;
		}
		HSpawn spawn = world.getGroupSpawns().get(group);
		if (spawn == null)
			spawn = world.getGroupSpawns().get(DEFAULT_GROUP);
		if (spawn == null) {
			gPlugin.getLogger().warning("[BUG] No spawns found for world " + worldName);
			return null;
		}
		return spawn;
	}

	public HSpawn getDefaultSpawn(String world) {
		return gWorlds.get(world).getGroupSpawns().get(DEFAULT_GROUP);
	}

	public boolean setSpawn(String group, HSpawn spawn) {
		spawn.setBedRespawn(getDefaultSpawn(spawn.getFromWorld()).getBedRespawn());
		spawn.setTpToSpawnOnJoin(getDefaultSpawn(spawn.getFromWorld()).getTpToSpawnOnJoin());
		HWorld hworld = gWorlds.get(spawn.getFromWorld());
		if (hworld == null)
			return false;
		hworld.getGroupSpawns().put(group, spawn);
		String fromWorld = spawn.getFromWorld();
		String keyX = String.format(KEY_FORMAT, fromWorld, group, "x");
		String keyY = String.format(KEY_FORMAT, fromWorld, group, "y");
		String keyZ = String.format(KEY_FORMAT, fromWorld, group, "z");
		String keyPitch = String.format(KEY_FORMAT, fromWorld, group, "pitch");
		String keyYaw = String.format(KEY_FORMAT, fromWorld, group, "yaw");
		String keySpawnWorld = String.format(KEY_FORMAT, fromWorld, group, "spawn_world");
		gSpawnsConf.set(keyX, spawn.getX());
		gSpawnsConf.set(keyY, spawn.getY());
		gSpawnsConf.set(keyZ, spawn.getZ());
		gSpawnsConf.set(keyPitch, (double) spawn.getPitch());
		gSpawnsConf.set(keyYaw, (double) spawn.getYaw());
		gSpawnsConf.set(keySpawnWorld, spawn.getSpawnWorld());
		gChange = true;
		saveSpawns();
		return true;
	}

	public boolean removeSpawn(String world, String group) {
		HWorld hworld = gWorlds.get(world);
		if (hworld == null)
			return false;
		if (!hworld.getGroupSpawns().containsKey(group))
			return false;
		gWorlds.remove(group);
		gSpawnsConf.set(world + "." + group, null);
		gChange = true;
		saveSpawns();
		return true;
	}

	public void saveSpawns() {
		if (!gChange)
			return;
		saveFile(gSpawnsConf, gSpawnsFile);
	}

	private void saveFile(YamlConfiguration conf, File file) {
		try {
			gPlugin.getLogger().info("Saving " + file.getName());
			file.getParentFile().mkdirs();
			file.createNewFile();
			conf.save(file);
			gChange = false;
		} catch (IOException ex) {
			gPlugin.getLogger().severe("Failed to save " + file.getName());
			ex.printStackTrace();
		}
	}

	private String getAssumedSpawnWorld(String world) {
		if (world.equalsIgnoreCase("world_nether") || world.equalsIgnoreCase("world_the_end"))
			return "world";
		return world;
	}
}
