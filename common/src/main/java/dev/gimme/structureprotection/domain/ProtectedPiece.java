package dev.gimme.structureprotection.domain;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * A protected structure piece as the client knows it: the structure's registry id and the piece's axis-aligned bounds.
 * This is the unit the server streams to the client and the unit the client {@code ClientStructureSource} tests
 * positions against. A pure value type, so reach-range snapshots compare by equality (to decide when to resend).
 */
public record ProtectedPiece(Identifier structure, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {

    public static ProtectedPiece of(Identifier structure, BoundingBox box) {
        return new ProtectedPiece(structure, box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ());
    }

    public boolean contains(BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
    }
}
