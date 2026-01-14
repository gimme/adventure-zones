package dev.gimme.adventurezones.infrastructure;

import dev.gimme.adventurezones.application.ChunkHandler;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import org.jetbrains.annotations.NotNull;

public class ChunkListener {

    private final ChunkHandler chunkHandler;

    public ChunkListener(@NotNull ChunkHandler chunkHandler) {
        this.chunkHandler = chunkHandler;
    }

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        chunkHandler.onChunkLoad(chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        chunkHandler.onChunkUnload(chunk);
    }
}
