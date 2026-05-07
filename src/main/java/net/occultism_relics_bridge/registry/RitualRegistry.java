package net.occultism_relics_bridge.registry;

import com.klikli_dev.occultism.common.ritual.RitualFactory;
import com.klikli_dev.occultism.registry.OccultismRituals;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.occultism_relics_bridge.OccultismRelicsBridge;
import net.occultism_relics_bridge.rituals.RitualAwakenRelic;

public class RitualRegistry {
    // Occultism nutzt einen eigenen Registry-Key für RitualFactories
    public static final DeferredRegister<RitualFactory> RITUAL_FACTORIES = 
            DeferredRegister.create(new ResourceLocation("occultism", "ritual_factory"), OccultismRelicsBridge.MODID);

    // Tier 1 - Foliot
    public static final RegistryObject<RitualFactory> AWAKEN_FOLIOT = RITUAL_FACTORIES.register("awaken_relic_foliot",
            () -> new RitualFactory((recipe) -> new RitualAwakenRelic(recipe, 1)));

    // Tier 2 - Djinn
    public static final RegistryObject<RitualFactory> AWAKEN_DJINN = RITUAL_FACTORIES.register("awaken_relic_djinn",
            () -> new RitualFactory((recipe) -> new RitualAwakenRelic(recipe, 2)));

    // Tier 3 - Marid
    public static final RegistryObject<RitualFactory> AWAKEN_MARID = RITUAL_FACTORIES.register("awaken_relic_marid",
            () -> new RitualFactory((recipe) -> new RitualAwakenRelic(recipe, 3)));

    public static void register(IEventBus eventBus) {
        RITUAL_FACTORIES.register(eventBus);
    }
}