package net.occultism_relics_bridge;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.PacketDistributor;
import net.occultism_relics_bridge.network.NetworkHandler;
import net.occultism_relics_bridge.network.SyncRelicConfigPacket;
import net.occultism_relics_bridge.registry.ItemRegistry;
import net.occultism_relics_bridge.registry.ParticleRegistry;
import net.occultism_relics_bridge.registry.RitualRegistry;
import net.occultism_relics_bridge.particles.DemonPaneParticle;
import net.occultism_relics_bridge.util.RelicConfigManager;
import org.slf4j.Logger;

@Mod(OccultismRelicsBridge.MODID)
public class OccultismRelicsBridge {
    public static final String MODID = "occultism_relics_bridge";
    private static final Logger LOGGER = LogUtils.getLogger();

    public OccultismRelicsBridge() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Alle Registries laden
        ItemRegistry.register(modEventBus);
        RitualRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Netzwerk registrieren
            NetworkHandler.register();
            // Stellt sicher, dass die Config beim Starten des Spiels/Servers direkt parat ist
            RelicConfigManager.initIfNeeded();
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == net.minecraft.world.item.CreativeModeTabs.INGREDIENTS) {
            event.accept(ItemRegistry.RELIC_PART);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_ARIES);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_TAURUS);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_GEMINI);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_CANCER);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_LEO);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_VIRGO);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_LIBRA);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_SCORPIO);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_SAGITTARIUS);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_CAPRICORN);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_AQUARIUS);
            event.accept(ItemRegistry.UNAWAKENED_ARTIFACT_PISCES);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) { }

    // --- Server -> Client Sync beim Einloggen ---
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), 
                    new SyncRelicConfigPacket(RelicConfigManager.getLocalConfigMap()));
        }
    }

    // Events, die AUSSCHLIESSLICH auf dem Client laufen dürfen (FORGE BUS)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        
        // Verwirft die Server-Config, wenn der Spieler sich ausloggt
        @SubscribeEvent
        public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
            RelicConfigManager.clearSync();
        }
    }

    // Events für das Setup von Client-Elementen (MOD BUS)
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        
        // Registriert die visuellen Renderer für unsere neuen Partikel
        @SubscribeEvent
        public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
            event.registerSpriteSet(ParticleRegistry.FOLIOT_PANE.get(), DemonPaneParticle.Provider::new);
            event.registerSpriteSet(ParticleRegistry.DJINN_PANE.get(), DemonPaneParticle.Provider::new);
            event.registerSpriteSet(ParticleRegistry.MARID_PANE.get(), DemonPaneParticle.Provider::new);
        }
    }
}