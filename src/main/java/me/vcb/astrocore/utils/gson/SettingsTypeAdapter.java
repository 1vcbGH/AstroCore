package me.vcb.astrocore.utils.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import me.vcb.astrocore.userdata.settings.Settings;
import me.vcb.astrocore.utils.StringUtils;

import java.lang.reflect.Type;

public class SettingsTypeAdapter implements JsonSerializer<Settings>, JsonDeserializer<Settings> {

    @Override
    public JsonElement serialize(Settings settings, Type type, JsonSerializationContext context) {
        return new JsonPrimitive(StringUtils.settingsToString(settings));
    }

    @Override
    public Settings deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        return StringUtils.settingsFromString(json.getAsString());
    }
}
