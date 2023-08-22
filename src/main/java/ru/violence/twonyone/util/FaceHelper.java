package ru.violence.twonyone.util;

import lombok.experimental.UtilityClass;
import org.bukkit.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class FaceHelper {
    public @NotNull BlockFace getCW(@NotNull BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.NORTH;
            default:
                throw new IllegalStateException("Unexpected value: " + face);
        }
    }

    public @NotNull BlockFace getCCW(@NotNull BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.EAST;
            case EAST:
                return BlockFace.NORTH;
            default:
                throw new IllegalStateException("Unexpected value: " + face);
        }
    }
}
