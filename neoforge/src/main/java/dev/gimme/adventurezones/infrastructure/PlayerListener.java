package dev.gimme.adventurezones.infrastructure;

import dev.gimme.adventurezones.application.PlayerHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListener {

    private final PlayerHandler playerHandler;

    public PlayerListener(@NotNull PlayerHandler playerHandler) {
        this.playerHandler = playerHandler;
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        playerHandler.onPlayerTick(serverPlayer);
    }
}
