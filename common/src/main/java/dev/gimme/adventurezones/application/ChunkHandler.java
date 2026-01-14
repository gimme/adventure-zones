package dev.gimme.adventurezones.application;

import dev.gimme.adventurezones.domain.AdventureZones;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

public class ChunkHandler {

    private final AdventureZones adventureZones;

    public ChunkHandler(@NotNull AdventureZones adventureZones) {
        this.adventureZones = adventureZones;
    }

    public void onChunkLoad(@NotNull LevelChunk chunk) {
        if (chunk.getLevel().isClientSide()) return;
        adventureZones.loadChunk(chunk);
    }

    public void onChunkUnload(@NotNull LevelChunk chunk) {
        if (chunk.getLevel().isClientSide()) return;
        adventureZones.unloadChunk(chunk.getPos());
    }
}
