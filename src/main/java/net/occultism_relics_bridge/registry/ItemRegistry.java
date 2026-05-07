package net.occultism_relics_bridge.registry;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.occultism_relics_bridge.OccultismRelicsBridge;
import net.occultism_relics_bridge.item.UnawakenedArtifactItem;
import net.occultism_relics_bridge.util.RelicGroupManager;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OccultismRelicsBridge.MODID);

    public static final RegistryObject<Item> RELIC_PART = ITEMS.register("relic_part",
            () -> new Item(new Item.Properties()));

    // --- Die 12 unbeseelten Artefakte nutzen jetzt unsere Custom Klasse ---

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_ARIES = ITEMS.register("unawakened_artifact_aries",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.ARIES));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_TAURUS = ITEMS.register("unawakened_artifact_taurus",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.TAURUS));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_GEMINI = ITEMS.register("unawakened_artifact_gemini",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.GEMINI));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_CANCER = ITEMS.register("unawakened_artifact_cancer",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.CANCER));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_LEO = ITEMS.register("unawakened_artifact_leo",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.LEO));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_VIRGO = ITEMS.register("unawakened_artifact_virgo",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.VIRGO));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_LIBRA = ITEMS.register("unawakened_artifact_libra",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.LIBRA));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_SCORPIO = ITEMS.register("unawakened_artifact_scorpio",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.SCORPIO));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_SAGITTARIUS = ITEMS.register("unawakened_artifact_sagittarius",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.SAGITTARIUS));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_CAPRICORN = ITEMS.register("unawakened_artifact_capricorn",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.CAPRICORN));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_AQUARIUS = ITEMS.register("unawakened_artifact_aquarius",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.AQUARIUS));

    public static final RegistryObject<Item> UNAWAKENED_ARTIFACT_PISCES = ITEMS.register("unawakened_artifact_pisces",
            () -> new UnawakenedArtifactItem(new Item.Properties(), RelicGroupManager.Zodiac.PISCES));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}