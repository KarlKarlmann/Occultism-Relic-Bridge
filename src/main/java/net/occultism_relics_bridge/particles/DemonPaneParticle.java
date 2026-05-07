package net.occultism_relics_bridge.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class DemonPaneParticle extends TextureSheetParticle {
    
    private final SpriteSet sprites;

    protected DemonPaneParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z, 0, 0, 0);
        this.sprites = sprites;
        this.hasPhysics = false; // Ignoriert Blöcke
        
        // Macht das Sprite riesig! (ca. 2x2 Blöcke groß)
        this.quadSize = 2.0F; 
        
        // Wir setzen die Lebensdauer extrem hoch (120 Sekunden = 2400 Ticks)
        // Das Ritual löscht den Partikel nicht selbst, er verschwindet einfach nach dem Ritual.
        this.lifetime = 60; 

        // Start-Frame
        this.setSprite(this.sprites.get(0, 3));
    }

    @Override
    public void tick() {
        super.tick();
        
        // Steuert die Animation: Wechselt alle 5 Ticks den Frame (0, 1, 2, 0, 1, 2...)
        int currentFrame = (this.age / 5) % 3;
        
        // Holt das richtige Bild aus dem SpriteSet
        this.setSprite(this.sprites.get(currentFrame, 3));
        
        // Leichtes Schweben nach oben
        this.yd = 0.005; 
        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    public ParticleRenderType getRenderType() {
        // Erlaubt transparente Texturen (wichtig für Geister!)
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; 
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, 
                                       double x, double y, double z, 
                                       double dx, double dy, double dz) {
            return new DemonPaneParticle(level, x, y, z, sprites);
        }
    }
}