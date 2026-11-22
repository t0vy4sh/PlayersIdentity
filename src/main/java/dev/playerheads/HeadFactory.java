package dev.playerheads;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tworzy komponent tekstowy typu "object/player" (główka gracza jako znak w tekście,
 * dodane w 1.21.9). Skin bierzemy z profilu, który serwer wysłał nam w liście graczy,
 * więc działa też z SkinsRestorer itp.
 */
public final class HeadFactory {
    private static final Map<String, Component> CACHE = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
            return size() > 256;
        }
    };

    private HeadFactory() {}

    /** @return komponent z główką albo null, jeśli się nie da go zbudować. */
    public static Component create(PlayerInfo info) {
        GameProfile profile = info.getProfile();
        if (profile == null || profile.name() == null || profile.name().isEmpty()) return null;

        boolean hat = Config.get().chatHeadHat;
        String key = profile.id() + "|" + profile.name() + "|" + hat + "|" + profile.properties().size();
        Component cached = CACHE.get(key);
        if (cached != null) return cached;

        JsonObject player = new JsonObject();
        player.addProperty("name", profile.name());

        UUID id = profile.id();
        if (id != null) {
            long msb = id.getMostSignificantBits();
            long lsb = id.getLeastSignificantBits();
            JsonArray arr = new JsonArray();
            arr.add((int) (msb >> 32));
            arr.add((int) msb);
            arr.add((int) (lsb >> 32));
            arr.add((int) lsb);
            player.add("id", arr);
        }

        JsonArray props = new JsonArray();
        for (Property p : profile.properties().values()) {
            if (!"textures".equals(p.name())) continue;
            JsonObject o = new JsonObject();
            o.addProperty("name", p.name());
            o.addProperty("value", p.value());
            if (p.signature() != null) o.addProperty("signature", p.signature());
            props.add(o);
        }
        if (!props.isEmpty()) player.add("properties", props);

        JsonObject root = new JsonObject();
        root.addProperty("object", "player");
        root.add("player", player);
        root.addProperty("hat", hat);

        Component result = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, root).result().orElse(null);
        if (result != null) CACHE.put(key, result);
        return result;
    }
}
