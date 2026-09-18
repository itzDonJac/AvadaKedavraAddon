package com.itzdonjac.avada_kedavra;

import com.itzdonjac.avada_kedavra.registry.ModSpells;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AvadaKedavraMod.MODID)
public final class AvadaKedavraMod {
    public static final String MODID = "avada_kedavra";

    public AvadaKedavraMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSpells.register(bus);
        MinecraftForge.EVENT_BUS.register(AvadaKedavraServerEvents.class);
    }
}
