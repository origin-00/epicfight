package yesman.epicfight.world.capabilities;

import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BaseCapability;

public class GlobalCapability<T> extends BaseCapability<T, Void> {
	public GlobalCapability(Identifier name, Class<T> type) {
		super(name, type, void.class);
	}
}
