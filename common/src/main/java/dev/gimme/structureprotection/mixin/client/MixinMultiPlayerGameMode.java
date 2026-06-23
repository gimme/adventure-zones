package dev.gimme.structureprotection.mixin.client;

import dev.gimme.structureprotection.application.BlockProtection;
import dev.gimme.structureprotection.client.ClientProtection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes protected blocks unminable on a client that has the mod: cancels the start and continuation of block breaking,
 * the same gate Adventure mode uses, so the player swings at air instead of cracking the block. Purely cosmetic — the
 * server still rejects the break regardless; this only removes the doomed mining feedback.
 */
@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {

    @Inject(method = "startDestroyBlock", at = @At("HEAD"), cancellable = true, require = 1)
    private void onStartDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (isProtected(pos)) cir.setReturnValue(false);
    }

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"), cancellable = true, require = 1)
    private void onContinueDestroy(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (isProtected(pos)) cir.setReturnValue(false);
    }

    private static boolean isProtected(BlockPos pos) {
        BlockProtection protection = ClientProtection.INSTANCE;
        if (protection == null) return false;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return false;
        return protection.preventsBreak(minecraft.level, pos, minecraft.player);
    }
}
