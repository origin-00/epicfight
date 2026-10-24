package yesman.epicfight.api.ex_cap.managers;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.resources.Identifier;
import yesman.epicfight.api.ex_cap.modules.core.data.ExCapData;
import yesman.epicfight.api.ex_cap.modules.core.events.ExCapabilityBuilderPopulationEvent;

import java.util.Map;
import java.util.List;

@Deprecated
public class ExCapManager
{
    private static final Map<Identifier, List<ExCapData.Builder>> dataMap = Maps.newHashMap();

    /**
     * Registers the preset builder as acceptor
     * @param cap The statically registered Capability Builder
     */
    public static void addAcceptor(Identifier cap)
    {
        dataMap.putIfAbsent(cap, Lists.newArrayList());
    }

    public static List<ExCapData.Builder> retrieveExCapData(Identifier cap)
    {
        return getDataMap().get(cap);
    }

    public static void addExCapData(Identifier cap, List<Identifier> data)
    {
        List<ExCapData.Builder> list = Lists.newArrayList();
        for (Identifier rl : data)
        {
            list.add(DatasetManager.get(rl));
        }
        dataMap.computeIfAbsent(cap, k -> Lists.newArrayList()).addAll(list);
    }

    public static Map<Identifier, List<ExCapData.Builder>> getDataMap()
    {
        return ImmutableMap.copyOf(dataMap);
    }

    public static void acceptEvent(ExCapabilityBuilderPopulationEvent event)
    {
        clearEntries();
        dataMap.putAll(event.getBuilders());
    }

    public static void clearEntries()
    {
        //Do not clear the actual keys, only clear the lists
        dataMap.forEach(ExCapManager::clearList);
    }

    private static void clearList(Identifier rl, List<ExCapData.Builder> list)
    {
        list.clear();
    }
}