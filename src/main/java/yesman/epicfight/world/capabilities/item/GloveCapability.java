package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.item.ItemStack;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.item.VanillaItemKinds;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

public class GloveCapability extends WeaponCapability {

	public GloveCapability(WeaponCapability.Builder builder) {
		super(builder);
	}
	
	@Override
	public boolean checkOffhandValid(LivingEntityPatch<?> entitypatch) {
		ItemStack offhandItme = entitypatch.getOriginal().getOffhandItem();
		CapabilityItem itemCap = EpicFightCapabilities.getItemStackCapability(offhandItme);
		boolean isFist = itemCap.getWeaponCategory() == WeaponCategories.FIST;
		return isFist || !(VanillaItemKinds.isSword(offhandItme.getItem()) || VanillaItemKinds.isDigger(offhandItme.getItem()));
	}
	
	@Override
	public boolean canHoldInOffhandAlone() {
		return true;
	}
}