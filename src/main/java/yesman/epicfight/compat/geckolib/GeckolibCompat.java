package yesman.epicfight.compat.geckolib;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.compat.ICompatModule;
import yesman.epicfight.compat.geckolib.client.GeoModelTransformer;

/// 26.1.2 port: only GeckoLib armor is supported. The GeckoLib entity render hooks were built on the
/// GeckoLib 4 render events, which GeckoLib 5 replaced with render-state based ones.
public class GeckolibCompat implements ICompatModule {
	@Override
	public void onModEventBusClient(IEventBus eventBus) {
		eventBus.<FMLClientSetupEvent>addListener(event -> event.enqueueWork(() -> HumanoidModelBaker.registerNewTransformer(new GeoModelTransformer())));
	}

	@Override
	public void onGameEventBusClient(IEventBus eventBus) {
        EpicFightClientEventHooks.Render.ANIMATED_ARMOR_TEXTURE.registerEvent(GeoModelTransformer::getGeoArmorTexturePath);
	}

	@Override
	public void onModEventBus(IEventBus eventBus) {
	}

	@Override
	public void onGameEventBus(IEventBus eventBus) {
	}
}
