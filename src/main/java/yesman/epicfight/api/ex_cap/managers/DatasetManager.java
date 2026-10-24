package yesman.epicfight.api.ex_cap.managers;

import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import com.google.gson.JsonElement;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapDataRegistrationEvent;

import java.util.Map;

@Deprecated
public class DatasetManager {
    private static final Map<Identifier, ExCapData.Builder> dataMap = Maps.newHashMap();

    public static void acceptEvent(ExCapDataRegistrationEvent event) {
        dataMap.clear();
        dataMap.putAll(event.getDataMap());
    }

    public static ExCapData.Builder get(Identifier resourceLocation)
    {
        return dataMap.get(resourceLocation);
    }

    public static void add(Identifier resourceLocation, JsonElement jsonElement)
    {
        dataMap.put(resourceLocation, ExCapData.Builder.deserialize(jsonElement));
    }
}