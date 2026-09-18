package com.itzdonjac.avada_kedavra.registry;

import com.itzdonjac.avada_kedavra.AvadaKedavraMod;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModSpells {
    private ModSpells() {}

    public static final DeferredRegister<AbstractSpell> SPELLS =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, AvadaKedavraMod.MODID);

    public static final RegistryObject<AbstractSpell> AVADA_KEDAVRA =
            SPELLS.register("avada_kedavra", AvadaKedavraSpell::new);

    public static void register(IEventBus bus) {
        SPELLS.register(bus);
    }
}
