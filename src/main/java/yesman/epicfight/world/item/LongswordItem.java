package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class LongswordItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createLongswordAttributes(ToolMaterial material) {
		return TieredWeaponItem.createAttributes(material, 4.0F, -2.8F, 0.0F);
	}
	
	public LongswordItem(ToolMaterial material, Item.Properties properties) {
		super(material, properties);
	}
}