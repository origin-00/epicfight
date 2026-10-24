package yesman.epicfight.api.ex_cap.listeners;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.FileToIdConverter;
import com.google.gson.JsonElement;
import com.google.common.collect.Lists;
import com.google.gson.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.EpicFight;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;
import yesman.epicfight.api.ex_cap.managers.ExCapManager;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.List;
import java.util.Map;

@Deprecated
public class ExCapDataReloadListener extends SimpleJsonResourceReloadListener<JsonElement>
{
    public static final String DIRECTORY = "capabilities/weapons/ex_cap_data/loader";

    private static final Gson GSON = (new GsonBuilder()).create();

    public ExCapDataReloadListener()
    {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(@NotNull Map<Identifier, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        ExCapabilityBuilderPopulationEvent exCapabilityBuilderPopulationEvent = new ExCapabilityBuilderPopulationEvent();
        EpicFightEventHooks.Registry.EX_CAP_DATA_POPULATION.post(exCapabilityBuilderPopulationEvent);
        ExCapManager.acceptEvent(exCapabilityBuilderPopulationEvent);
        elementMap.forEach(this::deserialize);
    }



    protected void deserialize(Identifier rl, JsonElement jsonElement) {
        EpicFight.LOGGER.info("Deserializing data for Ex Cap from JSON: {}", rl);
        JsonObject obj = jsonElement.getAsJsonObject();
        if (obj.has("target") && obj.get("target").isJsonPrimitive())
        {
            Identifier target = Identifier.tryParse(obj.get("target").getAsString().toLowerCase());
            if (target == null)
            {
                EpicFight.LOGGER.warn("Invalid target Identifier in Ex Cap data JSON: {}", rl);
                return;
            }
            if (obj.has("ex_cap_data") && obj.get("ex_cap_data").isJsonArray())
            {
                JsonArray exCapData = obj.getAsJsonArray("ex_cap_data");
                List<Identifier> exCapDataList = Lists.newArrayList();
                exCapData.asList().forEach(capData -> {
                    if (capData.isJsonPrimitive())
                    {
                        Identifier capDataRL = Identifier.tryParse(capData.getAsString().toLowerCase());
                        if (capDataRL != null)
                        {
                            exCapDataList.add(capDataRL);
                        }
                    }
                });
                ExCapManager.addExCapData(target, exCapDataList);
            }
        }
    }

    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.EX_CAP_INJECTION) {
            ExCapManager.acceptEvent(EpicFightEventHooks.Registry.EX_CAP_DATA_POPULATION.post(new ExCapabilityBuilderPopulationEvent()));
        }
    }
}