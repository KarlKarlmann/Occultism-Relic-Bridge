package net.occultism_relics_bridge.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;
import net.occultism_relics_bridge.util.RelicConfigManager;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncRelicConfigPacket {
    private final Map<ResourceLocation, String> configMap;

    public SyncRelicConfigPacket(Map<ResourceLocation, String> configMap) {
        this.configMap = configMap;
    }

    // Dekodieren (Empfangen)
    public SyncRelicConfigPacket(FriendlyByteBuf buf) {
        int size = buf.readInt();
        this.configMap = new HashMap<>();
        for (int i = 0; i < size; i++) {
            this.configMap.put(buf.readResourceLocation(), buf.readUtf());
        }
    }

    // Enkodieren (Senden)
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(configMap.size());
        configMap.forEach((key, value) -> {
            buf.writeResourceLocation(key);
            buf.writeUtf(value);
        });
    }

    // Was passiert, wenn das Paket auf dem Client ankommt
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Wir übergeben die Daten an unseren ConfigManager
            RelicConfigManager.applySyncPacket(configMap);
        });
        ctx.get().setPacketHandled(true);
    }
}