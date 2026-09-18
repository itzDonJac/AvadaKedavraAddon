package com.itzdonjac.avada_kedavra;

import com.itzdonjac.avada_kedavra.registry.ModSpells;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AvadaKedavraMod.MODID)
public class AvadaKedavraMod {
    public static final String MODID = "avada_kedavra";

    public AvadaKedavraMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModSpells.register(modEventBus);
    }
}
