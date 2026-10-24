package yesman.epicfight.api.ex_cap.managers;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import yesman.epicfight.api.ex_cap.modules.core.events.ConditionalRegistryEvent;
import yesman.epicfight.api.ex_cap.provider.ProviderConditional;
import net.minecraft.resources.Identifier;
import yesman.epicfight.registry.EpicFightRegistries;

import java.util.Map;

@ApiStatus.Experimental
public class ConditionalManager
{
    private static final Map<Identifier, ProviderConditional.Builder> CONDITIONALS = Maps.newHashMap();
    private static final Map<Identifier, ProviderConditional.Builder> BUILDER_DECLARED_CONDITIONALS = Maps.newHashMap();

    public static ProviderConditional.Builder get(Identifier id) {
        return CONDITIONALS.get(id);
    }

    public static void addConditional(Identifier id, ProviderConditional.Builder builder)
    {
        BUILDER_DECLARED_CONDITIONALS.put(id, builder);
    }

    public static void add(Identifier id, JsonElement json) {
        CONDITIONALS.put(id, ProviderConditional.Builder.deserialize(json));
    }

    @ApiStatus.Internal
    public static void acceptEvent(@Deprecated ConditionalRegistryEvent event)
    {

        CONDITIONALS.clear();
        EpicFightRegistries.PROVIDER_CONDITIONALS.entrySet().forEach(registryKey -> CONDITIONALS.put(registryKey.getKey().identifier(), registryKey.getValue()));
        CONDITIONALS.putAll(BUILDER_DECLARED_CONDITIONALS);
        CONDITIONALS.putAll(event.getConditionals());
    }

}
