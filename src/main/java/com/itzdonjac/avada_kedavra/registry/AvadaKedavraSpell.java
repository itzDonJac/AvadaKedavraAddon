package com.itzdonjac.avada_kedavra.registry;

import com.itzdonjac.avada_kedavra.AvadaKedavraServerEvents;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

@AutoSpellConfig
public final class AvadaKedavraSpell extends AbstractSpell {
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("avada_kedavra", "avada_kedavra");
    private static final float RANGE = 50.0F;

    private final DefaultConfig config = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(2)
            .setCooldownSeconds(10)
            .build();

    public AvadaKedavraSpell() {
        baseManaCost = 35;
        manaCostPerLevel = 5;
        baseSpellPower = 0;
        spellPowerPerLevel = 0;
        castTime = 0;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return ID;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return config;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData data) {
        return Utils.preCastTargetHelper(level, caster, data, this, RANGE, 0.4F);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getTotalDamage(spellLevel)),
                Component.translatable("ui.irons_spellbooks.distance", RANGE),
                Component.literal("4 colpi ogni 0,25 s; Wither 20 s")
        );
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource source, MagicData data) {
        if (level instanceof ServerLevel server && data.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
            LivingEntity target = targetData.getTarget(server);
            if (target != null && target.isAlive()) {
                float tickDamage = getTickDamage(spellLevel);

                // Impact at t=0, followed by one hit at 0.25, 0.50, 0.75 and 1.00 seconds.
                for (int strike = 0; strike < 5; strike++) {
                    int delay = strike == 0 ? 1 : strike * 5;
                    boolean finalStrike = strike == 4;
                    AvadaKedavraServerEvents.schedule(
                            caster, target,
                            strike == 0 ? getInitialDamage(spellLevel) : tickDamage,
                            delay,
                            true,
                            finalStrike
                    );
                }
            }
        }

        super.onCast(level, spellLevel, caster, source, data);
    }

    private static float getInitialDamage(int level) {
        return level <= 1 ? 100.0F : 200.0F;
    }

    private static float getTickDamage(int level) {
        return level <= 1 ? 30.0F : 60.0F;
    }

    private static float getTotalDamage(int level) {
        return getInitialDamage(level) + getTickDamage(level) * 4.0F;
    }
}
