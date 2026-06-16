package dev.gimme.adventurezones;

import dev.gimme.adventurezones.domain.config.ServerConfig;
import dev.gimme.adventurezones.domain.util.Constants;
import dev.gimme.adventurezones.infrastructure.ChunkListener;
import dev.gimme.adventurezones.infrastructure.NeoForgeServerConfig;
import dev.gimme.adventurezones.infrastructure.PlayerListener;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@Mod(Constants.MOD_ID)
public class NeoForgeMod {

    public NeoForgeMod(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NeoForgeServerConfig.SPEC, Constants.MOD_ID + "-server.toml");
        ServerConfig.INSTANCE = new NeoForgeServerConfig();

        // Register listeners once at mod load. They read the current Main.INSTANCE, which is
        // rebuilt per server start, so they never hold stale state across world loads.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new ChunkListener());
        NeoForge.EVENT_BUS.register(new PlayerListener());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        // Rebuild server-scoped state before any level/chunk loading begins.
        Main.init();
    }
}
