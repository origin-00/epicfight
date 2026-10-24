package yesman.epicfight.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class DaggerItem extends TieredWeaponItem {
	public static ItemAttributeModifiers createDaggerAttributes(ToolMaterial material) {
		return TieredWeaponItem.createAttributes(material, 1.0F, -1.6F, 0.0F);
	}
	
	public DaggerItem(ToolMaterial material, Item.Properties properties) {
		super(material, properties);
	}
}