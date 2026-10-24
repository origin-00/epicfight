package yesman.epicfight.data;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import yesman.epicfight.data.recipes.pack.EpicFightRecipeProvider;
import yesman.epicfight.data.tags.EpicFightBlockTagsProvider;
import yesman.epicfight.data.tags.EpicFightItemTagsProvider;
import yesman.epicfight.main.EpicFightMod;

@EventBusSubscriber(modid = EpicFightMod.MODID)
public final class DataEvents {
	private DataEvents() {}
	
	@SubscribeEvent
	public static void epicfight$gatherData(GatherDataEvent.Client event) {
		event.createProvider(EpicFightRecipeProvider.Runner::new);
		event.createProvider(EpicFightBlockTagsProvider::new);
		event.createProvider(EpicFightItemTagsProvider::new);
	}
}
