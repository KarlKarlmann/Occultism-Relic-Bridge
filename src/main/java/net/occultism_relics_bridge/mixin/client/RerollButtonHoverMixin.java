package net.occultism_relics_bridge.mixin.client;

import it.hurts.sskirillss.relics.client.screen.description.widgets.ability.RerollActionWidget;
import it.hurts.sskirillss.relics.client.screen.description.widgets.ability.base.AbstractActionWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Mixin(value = RerollActionWidget.class, remap = false)
public class RerollButtonHoverMixin {

    @Inject(method = "onHovered", at = @At("HEAD"), cancellable = true)
    private void onHoveredInject(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractActionWidget widget = (AbstractActionWidget) (Object) this;
        ItemStack stack = widget.getProvider().getStack();

        if (stack != null && stack.hasTag()) {
            CompoundTag nbt = stack.getTag();

            if (nbt.getBoolean("IsAwakenedByOccultism")) {
                String demonName = nbt.getString("PossessingDemon");
                if (demonName == null || demonName.isEmpty()) {
                    demonName = "Unbekannter Geist";
                }

                // Holt den Text und splittet ihn anhand von \n in mehrere Zeilen
                String fullText = Component.translatable("tooltip.occultism_relics_bridge.demon_stats_info", demonName).getString();
                String[] lines = fullText.split("\\\\n|\\n");

                List<Component> tooltipLines = new ArrayList<>();
                for (String line : lines) {
                    tooltipLines.add(Component.literal(line).withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC));
                }

                guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipLines, Optional.empty(), mouseX, mouseY);

                ci.cancel();
            }
        }
    }
}