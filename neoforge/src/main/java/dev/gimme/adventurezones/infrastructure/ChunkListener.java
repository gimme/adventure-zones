package dev.gimme.adventurezones.infrastructure;

import dev.gimme.adventurezones.Main;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;

public class ChunkListener {

    @SubscribeEvent
    public void onChunkLoad(ChunkEvent.Load event) {
        if (Main.INSTANCE == null) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        Main.INSTANCE.getChunkHandler().onChunkLoad(chunk);
    }

    @SubscribeEvent
    public void onChunkUnload(ChunkEvent.Unload event) {
        if (Main.INSTANCE == null) return;
        if (!(event.getChunk() instanceof LevelChunk chunk)) return;
        Main.INSTANCE.getChunkHandler().onChunkUnload(chunk);
    }
}
