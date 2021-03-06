package no.hon95.bukkit.hspawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;


public final class HSpawn {

	//TODO group field
	private double gX;
	private double gY;
	private double gZ;
	private float gPitch;
	private float gYaw;
	private String gWorld;
	private String gSpawnWorld;
	private boolean gBedRespawn;
	private boolean gTpToSpawnOnJoin;

	public HSpawn(double x, double y, double z, float pitch, float yaw, String spawnWorld, String world) {
		this(x, y, z, pitch, yaw, spawnWorld, world, true, false);
	}

	public HSpawn(double x, double y, double z, float pitch, float yaw, String spawnWorld, String world, boolean bedRespawn, boolean tpToSpwnOnJoin) {
		gX = x;
		gY = y;
		gZ = z;
		gPitch = pitch;
		gYaw = yaw;
		gWorld = world;
		gSpawnWorld = spawnWorld;
		gBedRespawn = bedRespawn;
		gTpToSpawnOnJoin = tpToSpwnOnJoin;
	}

	public double getX() {
		return gX;
	}

	public double getY() {
		return gY;
	}

	public double getZ() {
		return gZ;
	}

	public float getPitch() {
		return gPitch;
	}

	public float getYaw() {
		return gYaw;
	}

	public String getFromWorld() {
		return gWorld;
	}

	public String getSpawnWorld() {
		return gSpawnWorld;
	}

	public boolean getBedRespawn() {
		return gBedRespawn;
	}

	public boolean getTpToSpawnOnJoin() {
		return gTpToSpawnOnJoin;
	}

	public void setX(double x) {
		gX = x;
	}

	public void setY(double y) {
		gY = y;
	}

	public void setZ(double z) {
		gZ = z;
	}

	public void setPitch(float pitch) {
		gPitch = pitch;
	}

	public void setYaw(float yaw) {
		gYaw = yaw;
	}

	public void _setWorld(String world) {
		gWorld = world;
	}

	public void setFromWorld(String fromWorld) {
		gWorld = fromWorld;
	}

	public void setTpToSpawnOnJoin(boolean tpToSpawnOnJoin) {
		gTpToSpawnOnJoin = tpToSpawnOnJoin;
	}

	public void setBedRespawn(boolean bedRespawn) {
		gBedRespawn = bedRespawn;
	}

	public Location toLocation() {
		return new Location(Bukkit.getWorld(gWorld), gX, gY, gZ, gYaw, gPitch);
	}
}
