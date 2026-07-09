package com.github.Glatinis.lZBR.world.arena;

import com.github.Glatinis.lZBR.core.ConfigRepository;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.logging.Level;

// Resets the BR arena between rounds by pasting a schematic over it, avoiding a full world
// regeneration. Uses the WorldEdit API, which FastAsyncWorldEdit implements — with FAWE installed the
// paste runs asynchronously off the main thread, so a large arena rebuilds without freezing the server.
//
// FAWE is required: without it the paste would run on the main thread (unsafe for large edits), so the
// feature disables itself and logs a warning instead.
public class ArenaResetService {
    private static final String FAWE_PLUGIN = "FastAsyncWorldEdit";
    private static final String SCHEMATIC_DIR = "schematics";

    private final JavaPlugin plugin;
    private final ConfigRepository config;
    private final boolean faweInstalled;

    public ArenaResetService(JavaPlugin plugin, ConfigRepository config) {
        this.plugin = plugin;
        this.config = config;
        this.faweInstalled = plugin.getServer().getPluginManager().getPlugin(FAWE_PLUGIN) != null;

        // Make sure the folder players drop schematics into exists.
        new File(plugin.getDataFolder(), SCHEMATIC_DIR).mkdirs();

        if (!faweInstalled) {
            plugin.getLogger().warning(FAWE_PLUGIN + " not installed — arena reset is disabled.");
        }
    }

    // Whether a reset can actually run (FAWE present and the feature enabled in config).
    public boolean isAvailable() {
        return faweInstalled && config.isArenaResetEnabled();
    }

    // Pastes the configured schematic over the BR world. Runs asynchronously; onComplete is invoked on
    // the main thread with true on success, false if the reset was skipped or failed.
    public void reset(Consumer<Boolean> onComplete) {
        if (!isAvailable()) {
            onComplete.accept(false);
            return;
        }

        World world = Bukkit.getWorld(config.getBrWorldName());
        if (world == null) {
            plugin.getLogger().warning("Cannot reset arena: BR world '" + config.getBrWorldName() + "' is not loaded.");
            onComplete.accept(false);
            return;
        }

        File schematic = schematicFile();
        if (!schematic.isFile()) {
            plugin.getLogger().warning("Cannot reset arena: schematic not found at " + schematic.getPath());
            onComplete.accept(false);
            return;
        }

        BlockVector3 origin = BlockVector3.at(config.getArenaOriginX(), config.getArenaOriginY(), config.getArenaOriginZ());
        boolean pasteAir = config.isArenaPasteAir();
        boolean pasteEntities = config.isArenaPasteEntities();

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean success = paste(world, schematic, origin, pasteAir, pasteEntities);
            Bukkit.getScheduler().runTask(plugin, () -> onComplete.accept(success));
        });
    }

    private boolean paste(World world, File schematic, BlockVector3 origin, boolean pasteAir, boolean pasteEntities) {
        ClipboardFormat format = ClipboardFormats.findByFile(schematic);
        if (format == null) {
            plugin.getLogger().warning("Cannot reset arena: unrecognized schematic format for " + schematic.getName());
            return false;
        }

        try (InputStream in = new FileInputStream(schematic);
             ClipboardReader reader = format.getReader(in)) {
            Clipboard clipboard = reader.read();

            com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);
            try (EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(weWorld).build()) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(origin)
                        .ignoreAirBlocks(!pasteAir)
                        .copyEntities(pasteEntities)
                        .build();
                Operations.complete(operation);
            }
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to paste arena schematic " + schematic.getName(), e);
            return false;
        }
    }

    private File schematicFile() {
        return new File(new File(plugin.getDataFolder(), SCHEMATIC_DIR), config.getArenaSchematic());
    }
}
