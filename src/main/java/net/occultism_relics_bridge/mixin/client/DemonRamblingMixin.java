package net.occultism_relics_bridge.mixin.client;

import it.hurts.sskirillss.relics.client.screen.description.RelicDescriptionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.occultism_relics_bridge.OccultismRelicsBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

// Wir targeten die Vanilla "Screen" Klasse, um nervige Obfuskierungs-Warnungen
// von fremden Mods (wie Relics) beim Kompilieren komplett zu umgehen!
@Mixin(Screen.class)
public class DemonRamblingMixin {

    // Wir haben jetzt 100 Sätze (0 bis 99) in der Lang-Datei
    @Unique private static final int NUM_RAMBLINGS = 100;

    // Variablen für den Animal Crossing Effekt
    @Unique private static Screen lastScreen = null;
    @Unique private static String activeRambling = "";
    @Unique private static int visibleChars = 0;
    @Unique private static long lastTickTime = 0;

    // Wird jeden Frame am ENDE der Render-Methode (TAIL) aufgerufen.
    @Inject(method = "render", at = @At("TAIL"))
    public void onRenderDemonRambling(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        
        // Wir filtern hier heraus, ob der aktuelle Screen überhaupt der Relics-Screen ist!
        if ((Object) this instanceof RelicDescriptionScreen relicsScreen) {
            
            // Da die Variable 'stack' in der Relics-Klasse public ist, greifen wir direkt darauf zu
            ItemStack stack = relicsScreen.stack;

            if (stack == null || stack.isEmpty() || !stack.hasTag()) return;

            CompoundTag nbt = stack.getTag();
            
            // Prüfen, ob es ein von uns erwecktes Relikt ist
            if (nbt != null && nbt.getBoolean("IsAwakenedByOccultism")) {
                
                String demonName = nbt.getString("PossessingDemon");
                String demonType = nbt.getString("DemonType"); 
                // Den Namen des Spielers auslesen (Fallback, falls null)
                String playerName = Minecraft.getInstance().player != null ? Minecraft.getInstance().player.getName().getString() : "Sterblicher";
                
                // --- NEU: EINMALIG pro Menü-Öffnung triggern ---
                if (lastScreen != relicsScreen) {
                    lastScreen = relicsScreen;
                    
                    // 1. Wir nutzen den Dämonennamen als "Seed" für seinen Charakter
                    Random demonPersonality = new Random(demonName.hashCode());
                    
                    // 2. Erstelle einen persönlichen Pool von 12 Sätzen für GENAU diesen Dämon
                    int poolSize = 12;
                    int[] personalPool = new int[poolSize];
                    for (int i = 0; i < poolSize; i++) {
                        personalPool[i] = demonPersonality.nextInt(NUM_RAMBLINGS);
                    }
                    
                    // 3. Wähle beim Öffnen des UI einen zufälligen Satz aus SEINEM persönlichen Pool
                    int randomIndex = personalPool[(int) (Math.random() * poolSize)];
                    
                    // 4. Lade die Übersetzung und fülle %1$s (Dämon) und %2$s (Spieler) aus!
                    activeRambling = Component.translatable("occultism_relics_bridge.rambling." + randomIndex, demonName, playerName).getString();
                    
                    visibleChars = 0;
                    lastTickTime = System.currentTimeMillis();
                }

                // Anzahl der vorhandenen Bilder für diesen Typ bestimmen
                int maxImages = 10; 
                
                // Wir nutzen den Hash-Wert des Namens, damit derselbe Dämon IMMER dasselbe Gesicht bekommt
                int portraitIndex = Math.abs(demonName.hashCode()) % maxImages;
                
                // Textur-Pfad: "textures/portraits/djinn_3.png"
                ResourceLocation demonTexture = new ResourceLocation(OccultismRelicsBridge.MODID, "textures/portraits/" + demonType + "_" + portraitIndex + ".png");
                
                // --------------------------------------------------------
                // KOORDINATEN BERECHNEN (Exakt wie im Original Relics Code)
                // --------------------------------------------------------
                int backgroundWidth = 418;
                int backgroundHeight = 256;
                
                int x = (relicsScreen.width - backgroundWidth) / 2;
                int y = (relicsScreen.height - backgroundHeight) / 2;

                // Deine neuen Koordinaten (48x48) direkt oben am Menü!
                guiGraphics.blit(demonTexture, x + 60, y, 0, 0, 48, 48, 48, 48);

                // Zeichnet den Dämonennamen (Violett) direkt daneben
                guiGraphics.drawString(Minecraft.getInstance().font, "§5" + demonName, x + 112, y + 10, 0xFFFFFF, true);
                
                // --- NEU: ANIMAL CROSSING SCHREIBMASCHINEN-EFFEKT ---
                long currentTime = System.currentTimeMillis();
                // Alle 40ms wird ein neuer Buchstabe hinzugefügt
                if (visibleChars < activeRambling.length() && currentTime - lastTickTime > 40) {
                    visibleChars++;
                    lastTickTime = currentTime;

                    // Brabbel-Geräusch! Extrem leise und sehr hoch gepitcht
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.playSound(SoundEvents.ENDERMAN_AMBIENT, 0.15f, 1.8f + (float) Math.random() * 0.4f);
                    }
                }

                // Den aktuellen, getippten Teil des Textes ausschneiden
                String currentText = activeRambling.substring(0, visibleChars);
                
                // Zeichnet das Rambling (Grau, kursiv) genau unter den Namen
                guiGraphics.drawString(Minecraft.getInstance().font, "§7§o\"" + currentText + "\"", x + 112, y + 20, 0xFFFFFF, true);
            }
        } else {
            // Setzt die Animation zurück, sobald man den Screen verlässt
            lastScreen = null;
        }
    }
}