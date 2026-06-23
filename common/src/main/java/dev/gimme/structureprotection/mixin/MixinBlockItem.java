package dev.gimme.structureprotection.mixin;

import dev.gimme.structureprotection.Main;
import dev.gimme.structureprotection.application.BlockProtection;
import dev.gimme.structureprotection.client.ClientProtection;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels block placement that targets a protected structure piece. On the server this enforces protection; on a client
 * that has the mod it also cancels the <em>predicted</em> placement, so a protected spot shows no place-then-revert
 * flicker. The server side reads {@link Main}, the client side reads {@link ClientProtection} (null on a dedicated
 * server, where {@code isClientSide()} is false anyway).
 */
@Mixin(BlockItem.class)
public class MixinBlockItem {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true, require = 1)
    private void onPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        BlockProtection protection = context.getLevel().isClientSide()
                ? ClientProtection.INSTANCE
                : (Main.INSTANCE == null ? null : Main.INSTANCE.getBlockProtection());
        if (protection == null) return;

        if (protection.preventsPlace(context.getLevel(), context)) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
