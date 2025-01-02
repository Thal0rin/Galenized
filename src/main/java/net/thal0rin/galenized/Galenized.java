package net.thal0rin.galenized;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.thal0rin.galenized.init.GItems;
import org.slf4j.Logger;

@Mod(Galenized.MODID)
public class Galenized {

    public static final String MODID = "galenized";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Galenized() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GItems.register(eventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }
}
