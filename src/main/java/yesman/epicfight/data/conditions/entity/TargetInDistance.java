package yesman.epicfight.data.conditions.entity;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.datapack.ConditionParameterEditors;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class TargetInDistance extends EntityPatchCondition {
	private double min;
	private double max;
	
	public TargetInDistance() {
	}
	
	public TargetInDistance(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public TargetInDistance read(CompoundTag tag) {
		this.min = this.assertTag("min", "decimal", tag, NumericTag.class, (compoundTag, key) -> compoundTag.getDoubleOr(key, 0.0D));
		this.max = this.assertTag("max", "decimal", tag, NumericTag.class, (compoundTag, key) -> compoundTag.getDoubleOr(key, 0.0D));
		
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putDouble("min", this.min);
		tag.putDouble("max", this.max);
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		double distanceSqr = target.getOriginal().distanceToSqr(target.getTarget());
		return this.min * this.min < distanceSqr && distanceSqr < this.max * this.max;
	}
	
	@Override @ClientOnly
    @OnlyIn(Dist.CLIENT) // TODO: Remove OnlyIn annotation and completely decouple the widget provider code
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return ConditionParameterEditors.doubleRange(screen);
	}
}
