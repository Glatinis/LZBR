package com.github.Glatinis.lZBR.loot.chest;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

// A saved loot-chest position. facing is stored so the chest is restored the same way it was set,
// since a chest's opening direction is part of its block data. Chests only accept the four horizontal
// facings, so the constructor normalises anything else to NORTH.
public record ChestLocation(String world, int x, int y, int z, BlockFace facing) {

    public ChestLocation {
        facing = normalize(facing);
    }

    public static ChestLocation of(Block block, BlockFace facing) {
        return new ChestLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), facing);
    }

    public boolean isAt(Block block) {
        return block.getWorld().getName().equals(world)
                && block.getX() == x && block.getY() == y && block.getZ() == z;
    }

    public boolean samePosition(ChestLocation other) {
        return world.equals(other.world) && x == other.x && y == other.y && z == other.z;
    }

    private static BlockFace normalize(BlockFace facing) {
        return switch (facing) {
            case NORTH, EAST, SOUTH, WEST -> facing;
            default -> BlockFace.NORTH;
        };
    }
}
