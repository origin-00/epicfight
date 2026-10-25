package yesman.epicfight.data.conditions.entity;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.datapack.ConditionParameterEditors;
import yesman.epicfight.data.conditions.Condition.EntityPatchCondition;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.List;

public class OffhandItemCategory extends EntityPatchCondition {
	private WeaponCategory category;
	
	@Override
	public OffhandItemCategory read(CompoundTag tag) {
		this.category = this.assertExtensibleEnumTag("category", WeaponCategory.ENUM_MANAGER, tag);
		return this;
	}
	
	@Override
	public CompoundTag serializePredicate() {
		CompoundTag tag = new CompoundTag();
		tag.putString("category", this.category.toString());
		
		return tag;
	}
	
	@Override
	public boolean predicate(LivingEntityPatch<?> target) {
		return target.getHoldingItemCapability(InteractionHand.OFF_HAND).getWeaponCategory() == this.category;
	}

    @Override @ClientOnly
    @OnlyIn(Dist.CLIENT) // TODO: Remove OnlyIn annotation and completely decouple the widget provider code
	public List<ParameterEditor> getAcceptingParameters(Screen screen) {
		return ConditionParameterEditors.offhandItemCategory(screen);
	}
}