package dev.gimme.structureprotection.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.gimme.structureprotection.application.BlockProtection;
import dev.gimme.structureprotection.client.ClientProtection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the block selection outline on protected blocks, completing the Adventure-mode feel: a block you cannot mine
 * shows no wireframe either. Cancels the outline submission for the targeted block when protection applies, keyed to the
 * same break check so the outline and the mining feedback agree.
 */
@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {

    @Inject(method = "submitBlockOutline", at = @At("HEAD"), cancellable = true, require = 1)
    private void onSubmitBlockOutline(PoseStack poseStack, SubmitNodeCollector collector, LevelRenderState state,
                                      CallbackInfo ci) {
        BlockOutlineRenderState outline = state.blockOutlineRenderState;
        if (outline == null) return;

        BlockProtection protection = ClientProtection.INSTANCE;
        if (protection == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        if (protection.preventsBreak(minecraft.level, outline.pos(), minecraft.player)) {
            ci.cancel();
        }
    }
}
