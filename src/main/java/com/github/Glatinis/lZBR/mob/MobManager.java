package com.github.Glatinis.lZBR.mob;

import com.github.Glatinis.lZBR.core.MobRepository;
import com.github.Glatinis.lZBR.loot.LootManager;
import com.github.Glatinis.lZBR.world.zone.ZoneController;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

// Spawns a low density of ambient hostile mobs at configured points during a match, and hands them
// loot from the shared loot table when they die. All the density levers (alive cap, per-tick count,
// player-distance and zone gating) exist to keep mobs a supporting act so PvP stays primary.
public class MobManager implements Listener {
    private static final long TICKS_PER_SECOND = 20L;

    private final JavaPlugin plugin;
    private final Logger logger;
    private final MobRepository repository;
    private final LootManager lootManager;
    private final ZoneController zoneController;
    private final NamespacedKey mobTag;
    private final Random random = new Random();

    private final List<SpawnPoint> spawnPoints = new ArrayList<>();
    private List<MobType> mobTypes = new ArrayList<>();
    private final Set<UUID> spawnedMobs = new HashSet<>();

    private BukkitTask spawnTask;

    public MobManager(JavaPlugin plugin, MobRepository repository, LootManager lootManager,
                      ZoneController zoneController) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.repository = repository;
        this.lootManager = lootManager;
        this.zoneController = zoneController;
        this.mobTag = new NamespacedKey(plugin, "lzbr_mob");
        load();
    }

    public void reload() {
        repository.reload();
        load();
        // Reschedule with the new interval if a match is currently spawning.
        if (spawnTask != null) {
            stopSpawning();
            startTask();
        }
    }

    private void load() {
        spawnPoints.clear();
        spawnPoints.addAll(repository.loadSpawnPoints());
        mobTypes = loadMobTypes();
    }

    private List<MobType> loadMobTypes() {
        List<MobType> types = new ArrayList<>();
        var section = repository.getMobTypesSection();
        if (section == null) return types;

        for (String key : section.getKeys(false)) {
            EntityType type = resolveEntityType(key);
            if (type == null) {
                logger.warning("Unknown or non-living mob type '" + key + "' in mobs.yml — ignored.");
                continue;
            }
            double weight = section.getDouble(key + ".weight", 1.0);
            if (weight > 0) types.add(new MobType(type, weight));
        }
        return types;
    }

    private EntityType resolveEntityType(String key) {
        try {
            EntityType type = EntityType.valueOf(key.toUpperCase(Locale.ROOT));
            Class<?> entityClass = type.getEntityClass();
            if (!type.isSpawnable() || entityClass == null || !LivingEntity.class.isAssignableFrom(entityClass)) {
                return null;
            }
            return type;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // --- Match hooks ---------------------------------------------------------

    public void startSpawning() {
        stopSpawning();
        despawnAll();
        if (!repository.isEnabled() || spawnPoints.isEmpty() || mobTypes.isEmpty()) return;
        startTask();
    }

    private void startTask() {
        long interval = Math.max(1, repository.getIntervalSeconds()) * TICKS_PER_SECOND;
        spawnTask = Bukkit.getScheduler().runTaskTimer(plugin, this::spawnTick, interval, interval);
    }

    public void stopSpawning() {
        if (spawnTask != null) {
            spawnTask.cancel();
            spawnTask = null;
        }
    }

    // Removes every mob we spawned that is still around (match end, or crash recovery on next start).
    public void despawnAll() {
        for (UUID uuid : spawnedMobs) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) entity.remove();
        }
        spawnedMobs.clear();

        // Belt-and-braces: sweep spawn-point worlds for any tagged strays we lost track of.
        for (World world : spawnPointWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (isLzbrMob(entity)) entity.remove();
            }
        }
    }

    // --- Spawning ------------------------------------------------------------

    private void spawnTick() {
        pruneDead();
        if (spawnedMobs.size() >= repository.getMaxAlive()) return;

        int stopBelow = repository.getStopBelowZoneRadius();
        if (stopBelow > 0 && zoneController.isActive() && zoneController.getCurrentRadius() <= stopBelow) return;

        int min = Math.max(0, repository.getMinPerTick());
        int max = Math.max(min, repository.getMaxPerTick());
        int attempts = (min == max) ? min : min + random.nextInt(max - min + 1);

        for (int i = 0; i < attempts && spawnedMobs.size() < repository.getMaxAlive(); i++) {
            spawnOne(true);
        }
    }

    // For /lzbr test mob: spawns immediately, ignoring the player-distance and zone gates so an admin
    // standing on a point still sees a spawn. Respects the alive cap. Returns how many spawned.
    public int spawnBurst(int count) {
        pruneDead();
        int spawned = 0;
        for (int i = 0; i < count && spawnedMobs.size() < repository.getMaxAlive(); i++) {
            if (spawnOne(false)) spawned++;
        }
        return spawned;
    }

    private boolean spawnOne(boolean respectGates) {
        if (spawnPoints.isEmpty() || mobTypes.isEmpty()) return false;

        SpawnPoint point = spawnPoints.get(random.nextInt(spawnPoints.size()));
        World world = Bukkit.getWorld(point.world());
        if (world == null) return false;

        Location location = randomLocationAround(world, point);
        if (respectGates) {
            if (isPlayerNearby(location, repository.getMinPlayerDistance())) return false;
            if (!isInsideZone(location)) return false;
        }

        EntityType type = pickMobType();
        if (type == null) return false;

        Entity entity = world.spawnEntity(location, type);
        entity.getPersistentDataContainer().set(mobTag, PersistentDataType.BYTE, (byte) 1);
        spawnedMobs.add(entity.getUniqueId());
        return true;
    }

    private Location randomLocationAround(World world, SpawnPoint point) {
        int spread = Math.max(0, repository.getSpreadRadius());
        int offsetX = spread == 0 ? 0 : random.nextInt(spread * 2 + 1) - spread;
        int offsetZ = spread == 0 ? 0 : random.nextInt(spread * 2 + 1) - spread;
        // Centre of the block, at the spawn point's height so mobs appear where the point was placed.
        return new Location(world, point.x() + offsetX + 0.5, point.y(), point.z() + offsetZ + 0.5);
    }

    private EntityType pickMobType() {
        double total = mobTypes.stream().mapToDouble(MobType::weight).sum();
        if (total <= 0) return mobTypes.get(random.nextInt(mobTypes.size())).type();

        double target = random.nextDouble() * total;
        double cumulative = 0;
        for (MobType mobType : mobTypes) {
            cumulative += mobType.weight();
            if (target < cumulative) return mobType.type();
        }
        return mobTypes.get(mobTypes.size() - 1).type();
    }

    private boolean isPlayerNearby(Location location, double distance) {
        if (distance <= 0) return false;
        double distanceSquared = distance * distance;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) continue;
            if (player.getLocation().distanceSquared(location) <= distanceSquared) return true;
        }
        return false;
    }

    private boolean isInsideZone(Location location) {
        if (!zoneController.isActive()) return true;
        double dx = location.getX() - zoneController.getCenterX();
        double dz = location.getZ() - zoneController.getCenterZ();
        double radius = zoneController.getCurrentRadius();
        return dx * dx + dz * dz <= radius * radius;
    }

    // --- Drops ---------------------------------------------------------------

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isLzbrMob(entity)) return;

        spawnedMobs.remove(entity.getUniqueId());

        if (!repository.isKeepVanillaDrops()) {
            event.getDrops().clear();
        }

        if (random.nextDouble() * 100.0 >= repository.getDropChance()) return;

        int min = Math.max(0, repository.getMinDropRolls());
        int max = Math.max(min, repository.getMaxDropRolls());
        int rolls = (min == max) ? min : min + random.nextInt(max - min + 1);

        for (int i = 0; i < rolls; i++) {
            ItemStack loot = lootManager.rollLoot();
            if (loot != null) event.getDrops().add(loot);
        }
    }

    // --- In-game management --------------------------------------------------

    public boolean addSpawnPoint(Player player) {
        SpawnPoint point = SpawnPoint.of(player.getLocation());
        spawnPoints.add(point);
        repository.saveSpawnPoints(spawnPoints);
        return true;
    }

    // Removes the nearest spawn point within maxDistance blocks of the player. Returns the point
    // removed, or null if none were close enough.
    public SpawnPoint removeNearestSpawnPoint(Player player, double maxDistance) {
        Location location = player.getLocation();
        double bestDistance = maxDistance * maxDistance;
        SpawnPoint nearest = null;

        for (SpawnPoint point : spawnPoints) {
            double distanceSquared = point.distanceSquared(location);
            if (distanceSquared >= 0 && distanceSquared <= bestDistance) {
                bestDistance = distanceSquared;
                nearest = point;
            }
        }

        if (nearest != null) {
            spawnPoints.remove(nearest);
            repository.saveSpawnPoints(spawnPoints);
        }
        return nearest;
    }

    public List<SpawnPoint> getSpawnPoints() {
        return List.copyOf(spawnPoints);
    }

    public int getMobTypeCount() {
        return mobTypes.size();
    }

    // --- Helpers -------------------------------------------------------------

    private boolean isLzbrMob(Entity entity) {
        return entity.getPersistentDataContainer().has(mobTag, PersistentDataType.BYTE);
    }

    private void pruneDead() {
        spawnedMobs.removeIf(uuid -> {
            Entity entity = Bukkit.getEntity(uuid);
            return entity == null || entity.isDead();
        });
    }

    private Set<World> spawnPointWorlds() {
        Set<World> worlds = new HashSet<>();
        for (SpawnPoint point : spawnPoints) {
            World world = Bukkit.getWorld(point.world());
            if (world != null) worlds.add(world);
        }
        return worlds;
    }
}
