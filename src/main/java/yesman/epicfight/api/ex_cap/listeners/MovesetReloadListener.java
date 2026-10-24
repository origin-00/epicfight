package yesman.epicfight.api.ex_cap.listeners;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.FileToIdConverter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.event.EpicFightEventHooks;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapMovesetRegistryEvent;
import yesman.epicfight.api.ex_cap.managers.MovesetManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.network.server.SPDatapackSync;

import java.util.Map;
@ApiStatus.Experimental
public class MovesetReloadListener extends SimpleJsonResourceReloadListener<JsonElement>
{
    public static final String DIRECTORY = "capabilities/weapons/movesets";

    private static final Gson GSON = (new GsonBuilder()).create();

    public MovesetReloadListener()
    {
        super(ExtraCodecs.JSON, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(@NotNull Map<Identifier, JsonElement> elementMap, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller)
    {
        @Deprecated
        ExCapMovesetRegistryEvent event = createEvent();
        MovesetManager.acceptEvent(event);
        elementMap.forEach(MovesetManager::add);
    }

    @Deprecated
    private static ExCapMovesetRegistryEvent createEvent()
    {
        return EpicFightEventHooks.Registry.EX_CAP_MOVESET_REGISTRY.post(new ExCapMovesetRegistryEvent());
    }


    public static void processServerPacket(SPDatapackSync packet) {
        if (packet.packetType() == SPDatapackSync.PacketType.MOVESET) {
            @Deprecated
            ExCapMovesetRegistryEvent event = createEvent();
            MovesetManager.acceptEvent(event);
        }
    }
}
