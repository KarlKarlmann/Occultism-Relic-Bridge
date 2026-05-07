package net.occultism_relics_bridge.mixin;

import it.hurts.sskirillss.relics.level.RelicLootModifier;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.occultism_relics_bridge.registry.ItemRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// remap = false ist hier extrem wichtig, da LootModifier eine Forge-spezifische 
// Klasse ist und keine normalen Vanilla-Mappings (Obfuskierung) besitzt!
@Mixin(value = RelicLootModifier.class, remap = false)
public class RelicLootModifierMixin {

    // Wir greifen am ENDE der doApply-Methode ein, wenn Relics seinen Loot generiert hat
    @Inject(method = "doApply", at = @At("RETURN"), remap = false)
    private void replaceRelicDropsWithParts(ObjectArrayList<ItemStack> generatedLoot, LootContext context, CallbackInfoReturnable<ObjectArrayList<ItemStack>> cir) {
        
        ObjectArrayList<ItemStack> loot = cir.getReturnValue();
        if (loot == null || loot.isEmpty()) return;

        // Wir durchsuchen den generierten Loot nach Relics-Items
		for (int i = 0; i < loot.size(); i++) {
			ItemStack stack = loot.get(i);
			if (stack != null && !stack.isEmpty()) {
				
				// Neuer "intelligenter" Check über das Interface anstatt über den Namespace
				if (stack.getItem() instanceof it.hurts.sskirillss.relics.items.relics.base.IRelicItem) {
					
					// Wir überschreiben das Relikt gnadenlos mit unserem Relic Part!
					loot.set(i, new ItemStack(ItemRegistry.RELIC_PART.get(), 1));
				}
			}
		}
    }
}