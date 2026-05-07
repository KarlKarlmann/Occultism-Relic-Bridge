package net.occultism_relics_bridge.mixin;

import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.RelicItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = RelicItem.class, remap = false)
public abstract class PreventDemonRerollMixin implements IRelicItem {

    @Override
    public boolean mayPlayerReroll(Player player, ItemStack stack, String ability) {
        return false;
    }
}