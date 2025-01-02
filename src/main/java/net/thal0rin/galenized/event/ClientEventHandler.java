package net.thal0rin.galenized.event;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.thal0rin.galenized.Galenized;
import net.thal0rin.galenized.init.GItems;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(GItems.GOLDEN_LUMISENE_BUCKET.get(), new ResourceLocation(Galenized.MODID, "level"), (stack, world, entity, seed) -> {
            if (stack.hasTag() && stack.getTag().contains("FluidLevel")) {
                return stack.getTag().getInt("FluidLevel");
            }
            return 0;
        });
    }
}
