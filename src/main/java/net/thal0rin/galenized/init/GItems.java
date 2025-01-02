package net.thal0rin.galenized.init;

import com.teamabnormals.caverns_and_chasms.common.item.GoldenBucketItem;
import com.teamabnormals.caverns_and_chasms.core.registry.CCItems;
import net.mehvahdjukaar.supplementaries.reg.ModFluids;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.thal0rin.galenized.Galenized;

public class GItems {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, Galenized.MODID);

    public static RegistryObject<Item> GOLDEN_LUMISENE_BUCKET;

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);

        if (ModList.get().isLoaded("supplementaries")) {
            GOLDEN_LUMISENE_BUCKET = ITEMS.register("golden_lumisene_bucket", () -> new GoldenBucketItem(ModFluids.LUMISENE_FLUID, (new Item.Properties()).craftRemainder(CCItems.GOLDEN_BUCKET.get()).stacksTo(1)));
        }
    }
}
