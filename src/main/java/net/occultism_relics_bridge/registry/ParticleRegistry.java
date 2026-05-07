package net.occultism_relics_bridge.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.occultism_relics_bridge.OccultismRelicsBridge;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, OccultismRelicsBridge.MODID);

    public static final RegistryObject<SimpleParticleType> FOLIOT_PANE =
            PARTICLES.register("foliot_pane", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> DJINN_PANE =
            PARTICLES.register("djinn_pane", () -> new SimpleParticleType(true));

    public static final RegistryObject<SimpleParticleType> MARID_PANE =
            PARTICLES.register("marid_pane", () -> new SimpleParticleType(true));

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}