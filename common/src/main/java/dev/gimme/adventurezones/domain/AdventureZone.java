package dev.gimme.adventurezones.domain;

import dev.gimme.adventurezones.domain.config.ServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

class AdventureZone {

    private final SectionPos sectionPos;

    AdventureZone(BlockPos blockPos) {
        this.sectionPos = SectionPos.of(blockPos);
    }

    /**
     * Checks if the given position is inside this zone.
     */
    boolean covers(BlockPos pos) {
        var zoneRadius = ServerConfig.INSTANCE.getZoneRadius();
        var distance = getChessboardDistance(pos);
        return distance <= zoneRadius;
    }

    private int getChessboardDistance(BlockPos pos) {
        var blockSection = SectionPos.of(pos);

        var dx = Math.abs(this.sectionPos.getX() - blockSection.getX());
        var dy = Math.abs(this.sectionPos.getY() - blockSection.getY());
        var dz = Math.abs(this.sectionPos.getZ() - blockSection.getZ());

        return Math.max(dx, Math.max(dy, dz));
    }

    public SectionPos getSectionPos() {
        return sectionPos;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AdventureZone zone)) return false;
        return sectionPos.equals(zone.sectionPos);
    }

    @Override
    public int hashCode() {
        return sectionPos.hashCode();
    }

    static int getMaxPossibleChunkZoneRadius() {
        return ServerConfig.INSTANCE.getZoneRadius();
    }
}
