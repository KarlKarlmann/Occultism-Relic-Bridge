package net.occultism_relics_bridge.util;

import com.mojang.logging.LogUtils;
import it.hurts.sskirillss.relics.items.relics.base.IRelicItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RelicConfigManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("occultism_relics_bridge_zodiacs.cfg");
    
    // Stores the values explicitly set in the config (BANNED or Zodiac name)
    private static final Map<ResourceLocation, String> CONFIG_MAP = new HashMap<>();
    
    // Stores relics that are already in the file as comments to avoid duplicates
    private static final Set<ResourceLocation> COMMENTED_ITEMS = new HashSet<>();
    
    // Stores the values synced from the server (multiplayer)
    private static final Map<ResourceLocation, String> SERVER_SYNCED_MAP = new HashMap<>();
    private static boolean isSyncedFromServer = false;
    
    private static boolean isInitialized = false;

    public static void initIfNeeded() {
        if (isInitialized) return;
        isInitialized = true;

        loadConfig();
        scanAndAppendMissingRelics();
    }

    /**
     * Reads the config file. Captures both active lines and already commented out items.
     */
    private static void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            createDefaultConfig();
        }

        try {
            List<String> lines = Files.readAllLines(CONFIG_PATH);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Parse commented lines to capture already known (but inactive) items
                if (line.startsWith("#")) {
                    String cleanLine = line.substring(1).trim();
                    String[] parts = cleanLine.split("=");
                    if (parts.length == 2) {
                        try {
                            ResourceLocation itemId = new ResourceLocation(parts[0].trim());
                            COMMENTED_ITEMS.add(itemId);
                        } catch (Exception ignored) {
                            // Ignore plain text that happens to contain an '='
                        }
                    }
                    continue;
                }

                // Process active (not commented out) lines
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    try {
                        ResourceLocation itemId = new ResourceLocation(parts[0].trim());
                        String value = parts[1].trim().toUpperCase();
                        CONFIG_MAP.put(itemId, value);
                    } catch (Exception e) {
                        LOGGER.warn("Skipped invalid config line: " + line);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error loading the Occultism Relics Bridge config!", e);
        }
    }

    /**
     * Finds all relics that are NOT yet in the config (neither active nor as comments), 
     * automatically assigns them, and appends them to the end of the file!
     */
    private static void scanAndAppendMissingRelics() {
        List<String> linesToAppend = new ArrayList<>();
        boolean foundNew = false;

        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            if (item instanceof IRelicItem) {
                ResourceLocation registryName = ForgeRegistries.ITEMS.getKey(item);
                
                if (registryName != null) {
                    // Check if the item already exists in the file (neither active nor commented out)
                    if (!CONFIG_MAP.containsKey(registryName) && !COMMENTED_ITEMS.contains(registryName)) {
                        
                        if (!foundNew) {
                            linesToAppend.add("");
                            linesToAppend.add("# --- AUTO-DISCOVERED RELICS ---");
                            linesToAppend.add("# These relics were automatically assigned to a zodiac sign.");
                            linesToAppend.add("# Remove the '#' at the beginning of the line to change the assignment, or set it to BANNED.");
                            foundNew = true;
                        }

                        // Automatic fallback logic (Hash)
                        int hash = Math.abs(registryName.getPath().hashCode());
                        RelicGroupManager.Zodiac autoZodiac = RelicGroupManager.Zodiac.values()[hash % 12];
                        
                        // Add commented out line
                        linesToAppend.add("# " + registryName.toString() + " = " + autoZodiac.name());
                        
                        LOGGER.info("New relic discovered and added to the config (as a comment): " + registryName);
                    }
                }
            }
        }

        if (foundNew) {
            try {
                Files.write(CONFIG_PATH, linesToAppend, StandardOpenOption.APPEND);
            } catch (IOException e) {
                LOGGER.error("Could not write new relics to the config!", e);
            }
        }
    }

    private static void createDefaultConfig() {
        List<String> defaultLines = List.of(
                "# Occultism Relics Bridge - Zodiac Configuration",
                "# ==============================================",
                "# Here you can manually define which relic belongs to which zodiac sign.",
                "# Allowed zodiac signs: ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES",
                "# To completely remove a relic from the ritual pool, write: BANNED",
                "#",
                "# Example:",
                "# relics:rage_glove = ARIES",
                "# relics:ice_skates = BANNED",
                ""
        );
        try {
            Files.write(CONFIG_PATH, defaultLines, StandardOpenOption.CREATE_NEW);
        } catch (IOException e) {
            LOGGER.error("Could not create default config!", e);
        }
    }

    // --- NETWORK SYNC METHODS ---

    public static Map<ResourceLocation, String> getLocalConfigMap() {
        return CONFIG_MAP;
    }

    public static void applySyncPacket(Map<ResourceLocation, String> syncedMap) {
        SERVER_SYNCED_MAP.clear();
        SERVER_SYNCED_MAP.putAll(syncedMap);
        isSyncedFromServer = true;
        LOGGER.info("Occultism Relics Bridge: Received and applied config from server!");
    }

    public static void clearSync() {
        if (isSyncedFromServer) {
            SERVER_SYNCED_MAP.clear();
            isSyncedFromServer = false;
            LOGGER.info("Occultism Relics Bridge: Cleared server config (Logged out).");
        }
    }

    /**
     * Returns the configured value for an item (Zodiac sign name or "BANNED").
     * Always prioritizes the server config if we are in multiplayer!
     */
    public static String getConfiguredValue(ResourceLocation itemId) {
        if (isSyncedFromServer) {
            return SERVER_SYNCED_MAP.get(itemId);
        }
        return CONFIG_MAP.get(itemId);
    }
}