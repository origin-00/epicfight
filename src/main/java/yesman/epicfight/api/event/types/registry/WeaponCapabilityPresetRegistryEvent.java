package yesman.epicfight.api.event.types.registry;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import yesman.epicfight.api.event.Event;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Map;
import java.util.function.Function;

public class WeaponCapabilityPresetRegistryEvent extends Event {
	private final Map<Identifier, Function<Item, ? extends CapabilityItem.Builder<?>>> typeEntry;
	
	public WeaponCapabilityPresetRegistryEvent(Map<Identifier, Function<Item, ? extends CapabilityItem.Builder<?>>> typeEntry) {
		this.typeEntry = typeEntry;
	}
	
	public Map<Identifier, Function<Item, ? extends CapabilityItem.Builder<?>>> getTypeEntry() {
		return this.typeEntry;
	}
}