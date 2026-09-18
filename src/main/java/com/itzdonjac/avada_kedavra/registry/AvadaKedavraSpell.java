package com.itzdonjac.avada_kedavra.registry;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

@AutoSpellConfig
public class AvadaKedavraSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath("avada_kedavra", "avada_kedavra");

    private final DefaultConfig defaultConfig = new DefaultConfig()
        .setMinRarity(SpellRarity.LEGENDARY)
        .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
        .setMaxLevel(2)
        .setCooldownSeconds(10)
        .build();

    public AvadaKedavraSpell() {
        this.baseManaCost = 35;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 6;
        this.castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
            Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 2)),
            Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(getRange(), 1)),
            Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(1.5f, 1))
        );
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, this, 50, 0.4f);
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null && targetEntity.isAlive()) {
                float damage = getDamage(spellLevel, entity);
                targetEntity.hurt(getDamageSource(entity), damage);

                if (targetEntity.isAlive()) {
                    targetEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 30 * 20, 0, false, true));
                }

                for (int i = 0; i < 3; i++) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(world);
                    if (bolt != null) {
                        bolt.moveTo(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
                        world.addFreshEntity(bolt);
                    }
                }
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        float base = (spellLevel == 1) ? 100f : 200f;
        float bonus = (spellLevel == 1) ? 30f : 60f;
        return base + bonus;
    }

    public float getRange() {
        return 50f;
    }
}
