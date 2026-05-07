package net.occultism_relics_bridge.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.occultism_relics_bridge.OccultismRelicsBridge;

// Importiere das Interface aus der Relics Mod. 
// Hinweis: Je nach genauer Version liegt es unter .api.relics.IRelicItem oder .items.relics.base.IRelicItem
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem; 

import java.util.ArrayList;
import java.util.List;

/**
 * Verwaltet die Zuordnung von Relikten zu Sternzeichen.
 * Kombiniert automatische Zuweisung mit Datapack-Tags.
 */
public class RelicGroupManager {

    // Unser Enum für die 12 Sternzeichen
    public enum Zodiac {
        ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, 
        LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES;

        // Generiert automatisch den Tag-Key (z.B. "occultism_relics_bridge:zodiac/aries")
        public TagKey<Item> getTag() {
            return TagKey.create(Registries.ITEM, new ResourceLocation(OccultismRelicsBridge.MODID, "zodiac/" + this.name().toLowerCase()));
        }
    }

    /**
     * Gibt eine Liste aller Items zurück, die zu einem bestimmten Sternzeichen gehören.
     * Dies umfasst Items aus dem Datapack-Tag UND automatisch zugewiesene Relics.
     */
    public static List<Item> getItemsForZodiac(Zodiac targetZodiac) {
        List<Item> combinedItems = new ArrayList<>();

        // 1. DATAPACK-LOGIK: Lade alle Items, die manuell über Tags (JSON) hinzugefügt wurden
        var tagItems = ForgeRegistries.ITEMS.tags().getTag(targetZodiac.getTag());
        tagItems.forEach(combinedItems::add);

        // 2. AUTOMATISCHE LOGIK: Scanne alle existierenden Items im Spiel
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
            
            // INTELLIGENTER CHECK: Anstatt nach Namespaces ("relics", "artifacts", etc.) zu suchen,
            // prüfen wir einfach, ob das Item in Java das Relics-Interface implementiert!
            if (registryName != null && item instanceof IRelicItem) {
                
                // Falls ein Modpack-Macher dieses Relikt manuell über ein Tag einsortiert hat, 
                // überspringen wir die automatische Zuweisung, um Duplikate zu vermeiden.
                if (isItemInAnyZodiacTag(item)) {
                    continue; 
                }

                // Automatische Zuweisungs-Regel:
                // Wir nehmen den Hash-Wert des Item-Namens und nutzen Modulo 12.
                // Das sorgt dafür, dass jedes Relikt IMMER deterministisch exakt einem 
                // Sternzeichen zugeordnet wird. Selbst wenn Relics updatet, bleibt die Zuordnung stabil!
                int hash = Math.abs(registryName.getPath().hashCode());
                int zodiacIndex = hash % 12;
                
                Zodiac assignedZodiac = Zodiac.values()[zodiacIndex];

                // Wenn das automatisch berechnete Sternzeichen das gesuchte ist, füge es hinzu
                if (assignedZodiac == targetZodiac) {
                    if (!combinedItems.contains(item)) {
                        combinedItems.add(item);
                    }
                }
            }
        }

        return combinedItems;
    }

    /**
     * Hilfsmethode, um zu prüfen, ob ein Item bereits in IRGENDEINEM unserer Datapack-Tags steckt.
     */
    private static boolean isItemInAnyZodiacTag(Item item) {
        for (Zodiac zodiac : Zodiac.values()) {
            var tag = ForgeRegistries.ITEMS.tags().getTag(zodiac.getTag());
            if (tag.contains(item)) {
                return true;
            }
        }
        return false;
    }
}