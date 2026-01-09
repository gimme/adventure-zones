package dev.gimme.adventurezones.application;

import dev.gimme.adventurezones.domain.AdventureZones;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class PlayerHandler {

    private final AdventureZones adventureZones;

    public PlayerHandler(@NotNull AdventureZones adventureZones) {
        this.adventureZones = adventureZones;
    }

    public void onPlayerTick(@NotNull ServerPlayer player) {
        adventureZones.updatePlayer(player);
    }
}
