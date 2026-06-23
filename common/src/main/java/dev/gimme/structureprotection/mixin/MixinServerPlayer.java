package dev.gimme.structureprotection.mixin;

import dev.gimme.structureprotection.network.ProtectionSync;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drives the per-player protection sync from the server tick. Kept trivial — all the work and throttling live in
 * {@link ProtectionSync}, which no-ops for players whose client cannot receive the data.
 */
@Mixin(ServerPlayer.class)
public class MixinServerPlayer {

    @Inject(method = "tick", at = @At("TAIL"), require = 1)
    private void onTick(CallbackInfo ci) {
        ProtectionSync.tick((ServerPlayer) (Object) this);
    }
}
