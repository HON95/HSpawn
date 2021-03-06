package no.hon95.bukkit.hspawn;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.evilmidget38.UUIDFetcher;


public final class HCommandExecutor implements CommandExecutor {

	private static final String COMMAND = "spawn";
	private static final String PERM_SPAWN = "hspawn.command.spawn";
	private static final String PERM_SPAWN_OTHERS = "hspawn.command.spawnothers";
	private static final String PERM_SET_SPAWN = "hspawn.command.setspawn";
	private static final String PERM_REMOVE_SPAWN = "hspawn.command.removespawn";
	private static final String PERM_RELOAD = "hspawn.command.reload";
	private static final String PERM_HELP = "hspawn.command.reload";

	private final HSpawnPlugin gPlugin;

	public HCommandExecutor(HSpawnPlugin plugin) {
		gPlugin = plugin;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!command.getName().equalsIgnoreCase(COMMAND))
			return false;

		if (args.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("Only players may teleport to spawn");
				sender.sendMessage("Syntax: spawn [<player>|set|remove|reload|help]");
				return true;
			}
			if (!sender.hasPermission(PERM_SPAWN)) {
				sender.sendMessage(ChatColor.RED + "Permission denied!");
				return true;
			}
			tpToSpawn((Player) sender);
		} else if (args[0].equalsIgnoreCase("set")) {
			if (!sender.hasPermission(PERM_SET_SPAWN)) {
				sender.sendMessage(ChatColor.RED + "Permission denied!");
			}
			else if (args.length == 1) {
				sender.sendMessage("Syntax: spawn set <group> [world spawn_world x y z] [pitch yaw]");
				return true;
			}
			else if (args.length == 2) {
				if (!(sender instanceof Player))
					sender.sendMessage("Non-players need to specify the location");
				else
					setSpawn((Player) sender, args[1]);
			} else if (args.length == 6) {
				setSpawn(sender, args[1], args[2], args[3], args[4], args[5], args[6]);
			} else if (args.length == 8) {
				setSpawn(sender, args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
			}
		} else if (args[0].equalsIgnoreCase("remove")) {
			if (!sender.hasPermission(PERM_REMOVE_SPAWN)) {
				sender.sendMessage(ChatColor.RED + "Permission denied!");
			}
			else if (args.length == 1) {
				sender.sendMessage("Syntax: spawn remove <group> <world>");
				return true;
			}
			else if (args.length == 3) {
				removeSpawn(sender, args[1], args[2]);
			}
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission(PERM_RELOAD)) {
				sender.sendMessage(ChatColor.RED + "Permission denied!");
			}
			gPlugin.getConfigManager().reload();
			sender.sendMessage(ChatColor.GREEN + "hSpawn reloaded!");
		} else if (args[0].equalsIgnoreCase("help")) {
			if (!sender.hasPermission(PERM_HELP))
				sender.sendMessage(ChatColor.RED + "Permission denied!");
			else
				sender.sendMessage("Syntax: spawn [<player>|set|remove|reload|help]");
		} else {
			String name = args[0];
			if (!sender.hasPermission(PERM_SPAWN_OTHERS)) {
				sender.sendMessage(ChatColor.RED + "Permission denied!");
			} else {
				ArrayList<String> nameList = new ArrayList<String>();
				nameList.add(name);
				UUID uuid = null;
				try {
					Map<String, UUID> names = new UUIDFetcher(nameList).call();
					uuid = names.get(name);
					if (uuid == null)
						gPlugin.getLogger().warning("Failed to get " + name + "'s uuid using evilmidget38's UUIDFetcher");
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				Player player;
				try {
					player = Bukkit.getPlayer(uuid);
				} catch (NoSuchMethodError err) {
					// For CB 1.7.2-R0.3 support
					player = Bukkit.getPlayer(name);
				}
				if (player == null)
					sender.sendMessage(ChatColor.RED + "Player " + args[0] + " not found.");
				else
					sender.sendMessage(ChatColor.AQUA + "Teleporting " + player.getName() + " to spawn.");

				if (!tpToSpawn(player)) {
					sender.sendMessage(ChatColor.RED + "Failed to teleport player " + args[0] + " to spawn.");
					sender.sendMessage(ChatColor.RED + "The world is probably unloaded.");
				}
			}
		}

		return true;
	}

	private boolean tpToSpawn(Player player) {
		HSpawn spawn = gPlugin.getConfigManager().getSpawn(player);
		Location location = spawn.toLocation();
		if (location.getWorld() == null) {
			player.sendMessage(ChatColor.RED + "");
			gPlugin.getLogger().warning("World " + spawn.getSpawnWorld() + " is not found.");
			return false;
		}
		location = gPlugin.makeYSafe(spawn.toLocation());
		player.teleport(location, TeleportCause.COMMAND);
		player.sendMessage(ChatColor.AQUA + "You have been teleported to spawn.");
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
		return true;
	}

	private void setSpawn(Player player, String group) {
		Location loc = player.getLocation();
		setSpawn(player, group, loc.getWorld().getName(), loc.getWorld().getName(), loc.getX(), loc.getY(), loc.getZ(), loc.getPitch(), loc.getYaw());
	}

	private void setSpawn(CommandSender sender, String group, String world, String spawnWorld, String strX, String strY, String strZ) {
		setSpawn(sender, group, world, spawnWorld, strX, strY, strZ, "0", "0");
	}

	private void setSpawn(CommandSender sender, String group, String world, String spawnWorld, String strX, String strY, String strZ, String strPitch, String strYaw) {
		double x, y, z;
		float pitch, yaw;
		try {
			x = Double.parseDouble(strX);
			y = Double.parseDouble(strY);
			z = Double.parseDouble(strZ);
			pitch = Float.parseFloat(strPitch);
			yaw = Float.parseFloat(strYaw);
		} catch (NumberFormatException ex) {
			sender.sendMessage(ChatColor.RED + "Failed to set spawn, illegal numbers!");
			return;
		}
		setSpawn(sender, group, world, spawnWorld, x, y, z, pitch, yaw);
	}

	private void setSpawn(CommandSender sender, String group, String world, String spawnWorld, double x, double y, double z, float pitch, float yaw) {
		HSpawn spawn = new HSpawn(x, y, z, pitch, yaw, world, spawnWorld);
		if (gPlugin.getConfigManager().setSpawn(group, spawn))
			sender.sendMessage(ChatColor.GREEN + "Successfully set spawn for group " + group + " in world " + world + ".");
		else
			sender.sendMessage(ChatColor.RED + "Failed to set spawn for group " + group + " in world " + world + ".");
	}

	private void removeSpawn(CommandSender sender, String group, String world) {
		if (gPlugin.getConfigManager().removeSpawn(world, group))
			sender.sendMessage(ChatColor.GREEN + "Successfully removed spawn for group " + group + " in world " + world + ".");
		else
			sender.sendMessage(ChatColor.RED + "Spawn for group " + group + " in world " + world + " is not set.");
	}
}
