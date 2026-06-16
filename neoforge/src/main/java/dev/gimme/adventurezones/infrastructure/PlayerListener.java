package dev.gimme.adventurezones.infrastructure;

import dev.gimme.adventurezones.Main;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class PlayerListener {

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (Main.INSTANCE == null) return;
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        Main.INSTANCE.getPlayerHandler().onPlayerTick(serverPlayer);
    }

    @SubscribeEvent
    public void onPlayerCombat(LivingDamageEvent.Post event) {
        if (Main.INSTANCE == null) return;
        var sourceEntity = event.getSource().getEntity();
        var defender = event.getEntity();
        if (!(sourceEntity instanceof LivingEntity attacker)) return;

        Main.INSTANCE.getPlayerHandler().onLivingEntityAttack(attacker, defender);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAggroMob(LivingChangeTargetEvent event) {
        if (Main.INSTANCE == null) return;
        var target = event.getNewAboutToBeSetTarget();
        var mob = event.getEntity();

        if (event.getTargetType() != LivingChangeTargetEvent.LivingTargetType.MOB_TARGET) return;
        if (!(target instanceof ServerPlayer targetPlayer)) return;

        Main.INSTANCE.getPlayerHandler().onAggroMob(mob, targetPlayer);
    }
}
