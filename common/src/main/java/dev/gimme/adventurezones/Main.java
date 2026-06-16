package dev.gimme.adventurezones;

import dev.gimme.adventurezones.application.ChunkHandler;
import dev.gimme.adventurezones.application.PlayerHandler;
import dev.gimme.adventurezones.domain.AdventureZones;
import dev.gimme.adventurezones.domain.PlayerManager;

public class Main {

    public static Main INSTANCE;

    public static Main init() {
        INSTANCE = new Main();
        return INSTANCE;
    }

    private final ChunkHandler chunkHandler;
    private final PlayerHandler playerHandler;

    private Main() {
        AdventureZones adventureZones = new AdventureZones();
        PlayerManager playerManager = new PlayerManager(adventureZones);

        this.chunkHandler = new ChunkHandler(adventureZones);
        this.playerHandler = new PlayerHandler(playerManager);
    }

    public ChunkHandler getChunkHandler() {
        return chunkHandler;
    }

    public PlayerHandler getPlayerHandler() {
        return playerHandler;
    }
}
