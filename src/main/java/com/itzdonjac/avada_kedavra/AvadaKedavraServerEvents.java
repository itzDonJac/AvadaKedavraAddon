package com.itzdonjac.avada_kedavra;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class AvadaKedavraServerEvents {
    private static final List<PendingStrike> PENDING = new ArrayList<>();

    private AvadaKedavraServerEvents() {
    }

    public static void schedule(LivingEntity caster, LivingEntity target, float damage, int delay, boolean spawnVanillaLightning, boolean finalStrike) {
        synchronized (PENDING) {
            PENDING.add(new PendingStrike(
                    caster.getUUID(), target.getUUID(), target.level().dimension(), damage, delay,
                    spawnVanillaLightning, finalStrike));
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        synchronized (PENDING) {
            for (int i = PENDING.size() - 1; i >= 0; i--) {
                PendingStrike strike = PENDING.get(i);
                strike.ticksUntilHit--;

                ServerLevel level = event.getServer().getLevel(strike.dimension);
                LivingEntity caster = getLiving(level, strike.caster);
                LivingEntity target = getLiving(level, strike.target);

                if (caster != null && target != null && caster.isAlive() && target.isAlive()) {
                    // A lightning bolt entity is an instantaneous vanilla impact entity. The beam
                    // particles below make the bolt visibly travel from the caster's hand to target.
                    spawnHandToTargetLightning(level, caster, target, strike.spawnVanillaLightning);
                    applyTrueDamage(target, strike.damage);

                    if (strike.finalStrike && target.isAlive()) {
                        target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                net.minecraft.world.effect.MobEffects.WITHER, 20 * 20, 0, false, true, true));
                    }
                }

                if (strike.ticksUntilHit <= 0) {
                    PENDING.remove(i);
                }
            }
        }
    }

    private static LivingEntity getLiving(ServerLevel level, UUID uuid) {
        if (level == null) {
            return null;
        }
        Entity entity = level.getEntity(uuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    private static void spawnHandToTargetLightning(ServerLevel level, LivingEntity caster, LivingEntity target, boolean vanillaImpact) {
        double startX = caster.getX() - Math.sin(Math.toRadians(caster.getYRot())) * 0.35D;
        double startY = caster.getEyeY() - 0.25D;
        double startZ = caster.getZ() + Math.cos(Math.toRadians(caster.getYRot())) * 0.35D;
        double endX = target.getX();
        double endY = target.getY() + target.getBbHeight() * 0.5D;
        double endZ = target.getZ();

        // Vanilla electric particles along the complete hand-to-target line.
        for (int i = 0; i <= 24; i++) {
            double t = i / 24.0D;
            double x = startX + (endX - startX) * t;
            double y = startY + (endY - startY) * t;
            double z = startZ + (endZ - startZ) * t;
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 2, 0.04D, 0.04D, 0.04D, 0.02D);
        }

        if (vanillaImpact) {
            net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(endX, target.getY(), endZ);
                level.addFreshEntity(bolt);
            }
        }
    }

    /** Direct health modification bypasses armor, shields and normal damage reduction. */
    public static void applyTrueDamage(LivingEntity target, float amount) {
        if (!target.isAlive() || amount <= 0.0F) {
            return;
        }
        float remaining = target.getHealth() - amount;
        if (remaining <= 0.0F) {
            target.kill();
        } else {
            target.setHealth(remaining);
        }
    }

    private static final class PendingStrike {
        private final UUID caster;
        private final UUID target;
        private final ResourceKey<Level> dimension;
        private final float damage;
        private int ticksUntilHit;
        private final boolean spawnVanillaLightning;
        private final boolean finalStrike;

        private PendingStrike(UUID caster, UUID target, ResourceKey<Level> dimension, float damage,
                              int ticksUntilHit, boolean spawnVanillaLightning, boolean finalStrike) {
            this.caster = caster;
            this.target = target;
            this.dimension = dimension;
            this.damage = damage;
            this.ticksUntilHit = ticksUntilHit;
            this.spawnVanillaLightning = spawnVanillaLightning;
            this.finalStrike = finalStrike;
        }
    }
}
