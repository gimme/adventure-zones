package dev.gimme.structureprotection.fabric;

import dev.gimme.structureprotection.Main;
import dev.gimme.structureprotection.domain.config.ServerConfig;
import dev.gimme.structureprotection.domain.util.Constants;
import dev.gimme.structureprotection.infrastructure.FcapServerConfig;
import dev.gimme.structureprotection.network.ProtectionSync;
import dev.gimme.structureprotection.network.ProtectionUpdatePayload;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.config.ModConfig;

public class FabricMod implements ModInitializer {

    @Override
    public void onInitialize() {
        ConfigRegistry.INSTANCE.register(Constants.MOD_ID, ModConfig.Type.COMMON, FcapServerConfig.SPEC, FcapServerConfig.FILE_NAME);
        ServerConfig.INSTANCE = new FcapServerConfig();

        // Register the payload on both sides (server encodes, client decodes). The client receiver is wired separately
        // in the client entrypoint.
        PayloadTypeRegistry.clientboundPlay().register(ProtectionUpdatePayload.TYPE, ProtectionUpdatePayload.STREAM_CODEC);

        // Bridge the loader-agnostic sync to Fabric networking. canSend() is false for clients without the mod, so the
        // sync skips them entirely and protection stays server-authoritative.
        ProtectionSync.SENDER = new ProtectionSync.Sender() {
            @Override
            public boolean canSend(ServerPlayer player) {
                return ServerPlayNetworking.canSend(player, ProtectionUpdatePayload.TYPE);
            }

            @Override
            public void send(ServerPlayer player, ProtectionUpdatePayload payload) {
                ServerPlayNetworking.send(player, payload);
            }
        };

        // Protection is stateless and driven by mixins that read Main.INSTANCE, so wire it once at mod load.
        Main.init();
    }
}
