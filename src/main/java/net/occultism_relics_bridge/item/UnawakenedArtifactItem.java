package net.occultism_relics_bridge.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.occultism_relics_bridge.util.RelicGroupManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnawakenedArtifactItem extends Item {

    private final RelicGroupManager.Zodiac zodiac;

    public UnawakenedArtifactItem(Properties properties, RelicGroupManager.Zodiac zodiac) {
        super(properties);
        this.zodiac = zodiac;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);

        // Titelzeile für den Tooltip in Grau
        tooltipComponents.add(Component.translatable("tooltip.occultism_relics_bridge.potential_relics").withStyle(ChatFormatting.GRAY));

        // Dynamisch die Relikte für dieses spezifische Sternzeichen abrufen!
        List<Item> relics = RelicGroupManager.getItemsForZodiac(this.zodiac);

        if (relics.isEmpty()) {
            // Fallback, falls die Gruppe leer sein sollte
            tooltipComponents.add(Component.literal("  - ???").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            // Alle gefundenen Relikte der Gruppe auflisten
            for (Item relic : relics) {
                // Fügt den Namen des Relikts hinzu (z.B. "  - Rage Glove" in Dunkelviolett)
                tooltipComponents.add(Component.literal("  - ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(relic.getDescription().copy().withStyle(ChatFormatting.DARK_PURPLE)));
            }
        }
    }
}