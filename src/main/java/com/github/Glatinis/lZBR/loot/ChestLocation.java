package com.github.Glatinis.lZBR.loot;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.LinkedHashMap;
import java.util.Map;

// A saved loot-chest position. facing is stored so the chest can be re-placed the same way it was
// set, since a chest's opening direction is part of its block data.
public record ChestLocation(String world, int x, int y, int z, BlockFace facing) {

    public static ChestLocation of(Block block, BlockFace facing) {
        return new ChestLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(),
                normalizeFacing(facing));
    }

    public boolean isAt(Block block) {
        return block.getWorld().getName().equals(world)
                && block.getX() == x && block.getY() == y && block.getZ() == z;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("world", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("facing", facing.name());
        return map;
    }

    public static ChestLocation fromMap(Map<?, ?> map) {
        String world = String.valueOf(map.get("world"));
        int x = toInt(map.get("x"));
        int y = toInt(map.get("y"));
        int z = toInt(map.get("z"));
        BlockFace facing = parseFacing(map.get("facing"));
        return new ChestLocation(world, x, y, z, facing);
    }

    private static int toInt(Object value) {
        if (value instanceof Number number) return number.intValue();
        return Integer.parseInt(String.valueOf(value));
    }

    private static BlockFace parseFacing(Object value) {
        if (value == null) return BlockFace.NORTH;
        try {
            return normalizeFacing(BlockFace.valueOf(String.valueOf(value).toUpperCase()));
        } catch (IllegalArgumentException e) {
            return BlockFace.NORTH;
        }
    }

    // Chests only accept the four horizontal facings; fall back to NORTH for anything else.
    private static BlockFace normalizeFacing(BlockFace facing) {
        return switch (facing) {
            case NORTH, EAST, SOUTH, WEST -> facing;
            default -> BlockFace.NORTH;
        };
    }
}
