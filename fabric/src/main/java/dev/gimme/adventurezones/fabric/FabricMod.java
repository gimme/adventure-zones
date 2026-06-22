package dev.gimme.adventurezones.fabric;

import dev.gimme.adventurezones.Main;
import dev.gimme.adventurezones.domain.config.ServerConfig;
import dev.gimme.adventurezones.domain.util.Constants;
import dev.gimme.adventurezones.infrastructure.FcapServerConfig;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.config.ModConfig;

public class FabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.COMMON, FcapServerConfig.SPEC, FcapServerConfig.FILE_NAME);
        ServerConfig.INSTANCE = new FcapServerConfig();

        // Rebuild server-scoped state before any level/chunk loading begins, mirroring NeoForge's
        // ServerAboutToStartEvent. The listeners below read Main.INSTANCE, so they never hold stale state.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> Main.init());

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            if (Main.INSTANCE == null) return;
            Main.INSTANCE.getChunkHandler().onChunkLoad(chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            if (Main.INSTANCE == null) return;
            Main.INSTANCE.getChunkHandler().onChunkUnload(chunk);
        });

        // Fabric has no per-player tick event; iterate the player list once per server tick instead.
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (Main.INSTANCE == null) return;
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                Main.INSTANCE.getPlayerHandler().onPlayerTick(player);
            }
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (Main.INSTANCE == null) return;
            if (!(source.getEntity() instanceof LivingEntity attacker)) return;
            Main.INSTANCE.getPlayerHandler().onLivingEntityAttack(attacker, entity);
        });
    }
}
