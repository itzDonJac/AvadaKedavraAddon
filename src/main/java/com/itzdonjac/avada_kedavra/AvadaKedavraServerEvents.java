package com.itzdonjac.avada_kedavra;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AvadaKedavraServerEvents {
    private static final List<PendingDamage> PENDING = new ArrayList<>();

    private AvadaKedavraServerEvents() {}

    public static void schedule(LivingEntity target, float damage, int ticks) {
        synchronized (PENDING) {
            PENDING.add(new PendingDamage(target.getUUID(), target.level().dimension(), damage, ticks));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        synchronized (PENDING) {
            for (int i = PENDING.size() - 1; i >= 0; i--) {
                PendingDamage pending = PENDING.get(i);
                pending.ticksUntilHit--;
                if (pending.ticksUntilHit <= 0) {
                    LivingEntity target = find(event.getServer().getLevel(pending.dimension), pending.target);
                    if (target != null && target.isAlive()) {
                        applyTrueDamage(target, pending.damage);
                    }
                    PENDING.remove(i);
                }
            }
        }
    }

    private static LivingEntity find(ServerLevel level, UUID uuid) {
        if (level == null) return null;
        if (level.getEntity(uuid) instanceof LivingEntity living) return living;
        return null;
    }

    // Direct health reduction deliberately bypasses armor, shields and normal damage immunity.
    public static void applyTrueDamage(LivingEntity target, float amount) {
        if (!target.isAlive() || amount <= 0) return;
        float remaining = target.getHealth() - amount;
        if (remaining <= 0) target.kill();
        else target.setHealth(remaining);
    }

    private static final class PendingDamage {
        private final UUID target;
        private final net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension;
        private final float damage;
        private int ticksUntilHit;

        private PendingDamage(UUID target, net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension, float damage, int ticksUntilHit) {
            this.target = target;
            this.dimension = dimension;
            this.damage = damage;
            this.ticksUntilHit = ticksUntilHit;
        }
    }
}
