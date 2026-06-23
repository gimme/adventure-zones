package dev.gimme.structureprotection.client;

import dev.gimme.structureprotection.application.BlockProtection;

/**
 * Client-side composition root, mirroring {@link dev.gimme.structureprotection.Main}. Holds the {@link BlockProtection}
 * backed by streamed structure pieces, which the client mixins read to make protected blocks feel untouchable. Stays
 * {@code null} on a dedicated server (the client init that sets it never runs there), so readers must null-check it.
 */
public final class ClientProtection {

    public static BlockProtection INSTANCE;

    private ClientProtection() {
    }

    public static void init() {
        INSTANCE = new BlockProtection(new ClientStructureSource(ClientProtectedRegions.INSTANCE));
    }
}
