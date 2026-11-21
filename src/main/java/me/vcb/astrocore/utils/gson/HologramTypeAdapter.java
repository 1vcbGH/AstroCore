package me.vcb.astrocore.utils.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.vcb.astrocore.hologram.Hologram;
import me.vcb.astrocore.hologram.impl.LeaderboardHologram;
import me.vcb.astrocore.hologram.impl.StaticHologram;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HologramTypeAdapter implements JsonSerializer<List<Hologram>>, JsonDeserializer<List<Hologram>> {

    private static String getHologramType(Hologram hologram) {
        if(hologram instanceof LeaderboardHologram) return "LeaderboardHologram";
        return "StaticHologram";
    }

    private static Class<? extends Hologram> getHologramClass(String type) {
        switch(type) {
            case "LeaderboardHologram": return LeaderboardHologram.class;
            default: return StaticHologram.class;
        }
    }

    @Override
    public JsonElement serialize(List<Hologram> holograms, Type type, JsonSerializationContext context) {
        JsonArray array = new JsonArray();

        for(Hologram hologram : holograms) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", getHologramType(hologram));
            obj.add("data", context.serialize(hologram, hologram.getClass()));
            array.add(obj);
        }

        return array;
    }

    @Override
    public List<Hologram> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        List<Hologram> holograms = new ArrayList<>();

        for(JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();

            // Support both new format {"type":..,"data":{..}} and old format (raw hologram object)
            String hologramType;
            JsonElement dataElement;

            if(obj.has("data")) {
                hologramType = obj.has("type") ? obj.get("type").getAsString() : "StaticHologram";
                dataElement = obj.get("data");
            } else {
                // Old format: raw object — detect by presence of leaderboardType field
                hologramType = obj.has("leaderboardType") ? "LeaderboardHologram" : "StaticHologram";
                dataElement = obj;
            }

            Class<? extends Hologram> clazz = getHologramClass(hologramType);
            Hologram hologram = context.deserialize(dataElement, clazz);

            if(hologram == null) continue;

            hologram.initTransient();
            holograms.add(hologram);
        }

        return holograms;
    }
}
