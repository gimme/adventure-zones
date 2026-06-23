package dev.gimme.structureprotection.fabric.client;

import dev.gimme.structureprotection.client.ClientProtectedRegions;
import dev.gimme.structureprotection.client.ClientProtection;
import dev.gimme.structureprotection.network.ProtectionUpdatePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

/**
 * Client entrypoint: wires the client-side protection and receives streamed protection updates. Only runs on a physical
 * client, so it never touches the dedicated server. On a vanilla server (or one without this mod) no updates arrive and
 * the store stays empty, leaving vanilla behaviour intact.
 */
public class FabricClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientProtection.init();

        ClientPlayNetworking.registerGlobalReceiver(ProtectionUpdatePayload.TYPE, (payload, context) ->
                ClientProtectedRegions.INSTANCE.update(payload.rules(), payload.pieces()));
    }
}
