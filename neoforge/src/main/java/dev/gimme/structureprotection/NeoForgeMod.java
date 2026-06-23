package dev.gimme.structureprotection;

import dev.gimme.structureprotection.client.ClientProtectedRegions;
import dev.gimme.structureprotection.client.ClientProtection;
import dev.gimme.structureprotection.domain.config.ServerConfig;
import dev.gimme.structureprotection.domain.util.Constants;
import dev.gimme.structureprotection.infrastructure.FcapServerConfig;
import dev.gimme.structureprotection.network.ProtectionSync;
import dev.gimme.structureprotection.network.ProtectionUpdatePayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.extensions.ICommonPacketListener;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MOD_ID)
public class NeoForgeMod {

    public NeoForgeMod(ModContainer modContainer, IEventBus modEventBus) {
        modContainer.registerConfig(ModConfig.Type.COMMON, FcapServerConfig.SPEC, FcapServerConfig.FILE_NAME);
        ServerConfig.INSTANCE = new FcapServerConfig();

        modEventBus.addListener(NeoForgeMod::registerPayloads);

        // Bridge the loader-agnostic sync to NeoForge networking. hasChannel() is false for clients without the mod
        // (the payload is optional), so the sync skips them and protection stays server-authoritative.
        ProtectionSync.SENDER = new ProtectionSync.Sender() {
            @Override
            public boolean canSend(ServerPlayer player) {
                return ((ICommonPacketListener) player.connection).hasChannel(ProtectionUpdatePayload.TYPE);
            }

            @Override
            public void send(ServerPlayer player, ProtectionUpdatePayload payload) {
                PacketDistributor.sendToPlayer(player, payload);
            }
        };

        // Client-only: wire the client-side protection the client mixins read.
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientProtection.init();
        }

        // Protection is stateless and driven by mixins that read Main.INSTANCE, so wire it once at mod load.
        Main.init();
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        // optional(): clients without the mod still connect; they simply never negotiate this channel.
        PayloadRegistrar registrar = event.registrar(Constants.MOD_ID).optional();
        registrar.playToClient(ProtectionUpdatePayload.TYPE, ProtectionUpdatePayload.STREAM_CODEC,
                (payload, context) -> ClientProtectedRegions.INSTANCE.update(payload.rules(), payload.pieces()));
    }
}
