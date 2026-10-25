package yesman.epicfight.data.conditions.entity;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.datapack.ConditionParameterEditors;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class RandomChance extends EntityPatchCondition {
	private double chance;
	
	public RandomChance() {
		this.chance = 0.0D;
	}
	
	public RandomChance(double chance) {
		this.chance = chance;
	}
	
	@Override
	public RandomChance read(CompoundTag tag) {
		this.chance = this.assertTag("chance", "decimal", tag, NumericTag.class, (compoundTag, key) -> compoundTag.getDoubleOr(key, 0.0D));
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putDouble("chance", this.chance);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		return target.getOriginal().getRandom().nextDouble() < this.chance;
	}
	
	@Override @ClientOnly
    @OnlyIn(Dist.CLIENT) // TODO: Remove OnlyIn annotation and completely decouple the widget provider code
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return ConditionParameterEditors.randomChance(screen);
	}
}
