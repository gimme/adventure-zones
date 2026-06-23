package dev.gimme.structureprotection;

import dev.gimme.structureprotection.application.BlockProtection;
import dev.gimme.structureprotection.domain.ServerStructureSource;

/**
 * Composition root for the server side. Wires the stateless services and exposes them through {@link #INSTANCE}, which
 * the loader-agnostic server mixins read. {@link #init()} runs once at mod load — there is no per-server state to
 * rebuild. The client has its own composition root that backs the same {@link BlockProtection} with a streamed-piece
 * source.
 */
public class Main {

    public static Main INSTANCE;

    public static Main init() {
        INSTANCE = new Main();
        return INSTANCE;
    }

    private final BlockProtection blockProtection;

    private Main() {
        this.blockProtection = new BlockProtection(new ServerStructureSource());
    }

    public BlockProtection getBlockProtection() {
        return blockProtection;
    }
}
