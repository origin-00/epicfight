package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class GloveItem extends WeaponItem {
	/// Enchantability and the iron-nugget repair material used to be method overrides; since
	/// 1.21.2 they are item properties.
	public static Item.Properties properties(Item.Properties properties) {
		return swordBehavior(properties).enchantable(22).repairable(Items.IRON_NUGGET);
	}
	
	public GloveItem(Item.Properties properties) {
		super(properties);
	}
}
