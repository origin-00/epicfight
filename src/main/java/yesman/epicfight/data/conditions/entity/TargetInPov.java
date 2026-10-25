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

public class TargetInPov extends EntityPatchCondition {
	protected double min;
	protected double max;
	
	public TargetInPov() {
	}
	
	public TargetInPov(double min, double max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public TargetInPov read(CompoundTag tag) {
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
	public boolean predicate(LivingEntityPatch<?> entitypatch) {
		double degree = entitypatch.getAngleTo(entitypatch.getTarget());
		return this.min < degree && degree < this.max;
	}
	
	@Override @ClientOnly
    @OnlyIn(Dist.CLIENT) // TODO: Remove OnlyIn annotation and completely decouple the widget provider code
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return ConditionParameterEditors.doubleRange(screen);
	}
	
	public static class TargetInPovHorizontal extends TargetInPov {
		public TargetInPovHorizontal() {
		}
		
		public TargetInPovHorizontal(double min, double max) {
			super(min, max);
		}
		
		@Override
		public boolean predicate(LivingEntityPatch<?> entitypatch) {
			double degree = entitypatch.getAngleToHorizontal(entitypatch.getTarget());
			return this.min < degree && degree < this.max;
		}
	}
}
