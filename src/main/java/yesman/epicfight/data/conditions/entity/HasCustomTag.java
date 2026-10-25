package yesman.epicfight.data.conditions.entity;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.datapack.ConditionParameterEditors;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.Condition.EntityCondition;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class HasCustomTag extends EntityCondition {
	private final Set<String> allowedTags;
	
	public HasCustomTag(ListTag allowedTags) {
		this.allowedTags = allowedTags.stream().map(ParseUtil::tagAsString).collect(Collectors.toUnmodifiableSet());
	}
	
	@Override
	public Condition<Entity> read(CompoundTag tag) {
		// This condition doesn't support json conversion
		return null;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		return null;
	}
	
	@Override
	public boolean predicate(Entity target) {
		for (String tag : this.allowedTags) {
			if (target.entityTags().contains(tag)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override @ClientOnly
    @OnlyIn(Dist.CLIENT) // TODO: Remove OnlyIn annotation and completely decouple the widget provider code
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return ConditionParameterEditors.customTag(screen);
	}
}