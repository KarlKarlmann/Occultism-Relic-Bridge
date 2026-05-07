package net.occultism_relics_bridge.rituals;

import com.klikli_dev.occultism.common.blockentity.GoldenSacrificialBowlBlockEntity;
import com.klikli_dev.occultism.common.ritual.Ritual;
import com.klikli_dev.occultism.crafting.recipe.RitualRecipe;
import com.klikli_dev.occultism.util.ItemNBTUtil;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.AbilityData;
import it.hurts.sskirillss.relics.items.relics.base.data.leveling.StatData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.occultism_relics_bridge.registry.ParticleRegistry;
import net.occultism_relics_bridge.util.RelicGroupManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class RitualAwakenRelic extends Ritual {

    private final int tier; // 1 = Foliot, 2 = Djinn, 3 = Marid

    public RitualAwakenRelic(RitualRecipe recipe, int tier) {
        super(recipe);
        this.tier = tier;
    }

    @Override
    public void finish(Level level, BlockPos goldenBowlPosition, GoldenSacrificialBowlBlockEntity blockEntity,
                       @Nullable ServerPlayer castingPlayer, ItemStack activationItem) {

        // Wir kopieren das Buch (Activation Item), bevor es vom Ritual verbraucht wird
        ItemStack copy = activationItem.copy();
        
        // Führt das eigentliche Ritual aus (konsumiert die Zutaten auf den äußeren Podesten)
        super.finish(level, goldenBowlPosition, blockEntity, castingPlayer, activationItem);

        // FIX: Occultism löscht das Aktivierungsitem in der Mitte NICHT automatisch.
        activationItem.shrink(1);

        if (level.isClientSide) return;

        // --- Dämonen-Partikel und Fancy Effekte am ENDE spawnen ---
        ServerLevel serverLevel = (ServerLevel) level;
        
        net.minecraft.core.particles.SimpleParticleType particleType = ParticleRegistry.FOLIOT_PANE.get();
        if (this.tier == 2) particleType = ParticleRegistry.DJINN_PANE.get();
        if (this.tier == 3) particleType = ParticleRegistry.MARID_PANE.get();

        serverLevel.sendParticles(particleType, 
                goldenBowlPosition.getX() + 0.5, goldenBowlPosition.getY() + 1.5, goldenBowlPosition.getZ() + 0.5, 
                1, 0, 0, 0, 0);

        serverLevel.sendParticles(ParticleTypes.WITCH, 
                goldenBowlPosition.getX() + 0.5, goldenBowlPosition.getY() + 1.0, goldenBowlPosition.getZ() + 0.5, 
                30, 0.5, 0.5, 0.5, 0.1);
        
        serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, 
                goldenBowlPosition.getX() + 0.5, goldenBowlPosition.getY() + 1.0, goldenBowlPosition.getZ() + 0.5, 
                20, 0.3, 0.3, 0.3, 0.05);
        
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, 
                goldenBowlPosition.getX() + 0.5, goldenBowlPosition.getY() + 1.0, goldenBowlPosition.getZ() + 0.5, 
                15, 0.4, 0.4, 0.4, 0.05);

        serverLevel.playSound(null, goldenBowlPosition, SoundEvents.ENDERMAN_TELEPORT, SoundSource.BLOCKS, 1.0F, 0.5F);
        serverLevel.playSound(null, goldenBowlPosition, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 1.0F, 0.8F);
        // ----------------------------------------------

        // 1. Dämonennamen auslesen
        String demonName = ItemNBTUtil.getBoundSpiritName(copy);
        if (demonName == null || demonName.isEmpty()) {
            demonName = "Unknown Spirit";
        }

        // 2. Sternzeichen & Relikt Pool bestimmen
        ItemStack resultItem = this.recipe.getResultItem(level.registryAccess());
        ResourceLocation resultId = ForgeRegistries.ITEMS.getKey(resultItem.getItem());
        if (resultId == null) return;
        
        String itemName = resultId.getPath();
        RelicGroupManager.Zodiac zodiac = determineZodiacFromName(itemName);
        if (zodiac == null) return;

        List<Item> relicPool = RelicGroupManager.getItemsForZodiac(zodiac);
        if (relicPool.isEmpty()) return; 

        // 3. Zufälliges Relikt wählen
        Random random = new Random();
        Item selectedRelic = relicPool.get(random.nextInt(relicPool.size()));
        ItemStack awakenedRelic = new ItemStack(selectedRelic);

        // 4. Namen anwenden
        String relicBaseName = selectedRelic.getDescription().getString();
        awakenedRelic.setHoverName(Component.literal("§5" + demonName + "§r, The " + relicBaseName));

        // 5. NBT Tag für unsere UI markieren
        CompoundTag nbt = awakenedRelic.getOrCreateTag();
        nbt.putString("PossessingDemon", demonName);
        nbt.putString("DemonType", getDemonTierName(this.tier));
        nbt.putBoolean("IsAwakenedByOccultism", true);
        awakenedRelic.setTag(nbt);

        // 6. Die echten Relics-Basis-Stats auswürfeln (nutzt das IRelicItem Interface!)
        if (selectedRelic instanceof IRelicItem relicInterface) {
            applyDemonStats(awakenedRelic, relicInterface, this.tier, random);
        }

        // 7. Das fertige Relikt spawnen
        this.spawnEntity(awakenedRelic, level, goldenBowlPosition);
    }

    /**
     * Setzt die Basis-Werte des Relikts abhängig vom Dämonen-Tier.
     * Nutzt die nativen Relics-Methoden, sodass kein Auto-Reroll mehr beim Aufheben getriggert wird.
     */
    private void applyDemonStats(ItemStack stack, IRelicItem relic, int tier, Random random) {
        Map<String, AbilityData> abilities = relic.getAbilitiesData().getAbilities();

        for (Map.Entry<String, AbilityData> abilityEntry : abilities.entrySet()) {
            String abilityName = abilityEntry.getKey();
            Map<String, StatData> stats = abilityEntry.getValue().getStats();

            for (Map.Entry<String, StatData> statEntry : stats.entrySet()) {
                String statName = statEntry.getKey();
                StatData statData = statEntry.getValue();

                double min = statData.getInitialValue().getKey();
                double max = statData.getInitialValue().getValue();
                double range = max - min;
                double value;

                if (range == 0) {
                    value = min;
                } else {
                    // Tier 1: 0% bis 40% des maximalen Potenzials
                    // Tier 2: 40% bis 80% des maximalen Potenzials
                    // Tier 3: 80% bis 100% des maximalen Potenzials (Nahezu perfekte Stats!)
                    double minFactor = (tier == 1) ? 0.0 : (tier == 2) ? 0.4 : 0.8;
                    double maxFactor = (tier == 1) ? 0.4 : (tier == 2) ? 0.8 : 1.0;

                    value = min + range * (minFactor + random.nextDouble() * (maxFactor - minFactor));
                }

                // Auf 5 Nachkommastellen runden, genau wie die Relics Mod es intern tut
                value = Math.round(value * 100000.0) / 100000.0;

                // Setzt den berechneten Wert direkt über das Interface in die NBT-Daten des Items!
                relic.setAbilityValue(stack, abilityName, statName, value);
            }
        }
    }

    private RelicGroupManager.Zodiac determineZodiacFromName(String name) {
        for (RelicGroupManager.Zodiac z : RelicGroupManager.Zodiac.values()) {
            if (name.contains(z.name().toLowerCase())) {
                return z;
            }
        }
        return null;
    }

    private String getDemonTierName(int tier) {
        if (tier == 1) return "foliot";
        if (tier == 2) return "djinn";
        if (tier == 3) return "marid";
        return "unknown";
    }

    private void spawnEntity(ItemStack stack, Level level, BlockPos pos) {
        net.minecraft.world.entity.item.ItemEntity entity = new net.minecraft.world.entity.item.ItemEntity(
                level, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, stack
        );
        entity.setDeltaMovement(0, 0.1, 0); // Leichtes "Ploppen" nach oben
        level.addFreshEntity(entity);
    }
}