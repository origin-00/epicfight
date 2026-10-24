package yesman.epicfight.registry.entries;

import java.util.EnumMap;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import yesman.epicfight.main.EpicFightMod;

/// Since 1.21.2 an [ArmorMaterial] is a plain record rather than a registry entry, and its
/// textures are described by an equipment asset (`assets/epicfight/equipment/<name>.json`).
public final class EpicFightArmorMaterials {
	private EpicFightArmorMaterials() {}
	
	public static final TagKey<Item> REPAIRS_STRAY_CLOTH_ARMOR = TagKey.create(Registries.ITEM, EpicFightMod.identifier("repairs_stray_cloth_armor"));
	public static final ResourceKey<EquipmentAsset> STRAY_CLOTH_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, EpicFightMod.identifier("stray_cloth"));
	
	public static final ArmorMaterial STRAY_CLOTH = new ArmorMaterial(
		  0
		, Util.make(
			new EnumMap<>(ArmorType.class),
			enumMap -> {
		        enumMap.put(ArmorType.BOOTS, 1);
		        enumMap.put(ArmorType.LEGGINGS, 2);
		        enumMap.put(ArmorType.CHESTPLATE, 3);
		        enumMap.put(ArmorType.HELMET, 1);
		        enumMap.put(ArmorType.BODY, 2);
			}
		)
		, 15
		, SoundEvents.ARMOR_EQUIP_LEATHER
		, 0.0F
		, 0.0F
		, REPAIRS_STRAY_CLOTH_ARMOR
		, STRAY_CLOTH_ASSET
	);
	
	/// [Item.Properties#humanoidArmor] without the durability: the stray set has always been
	/// unbreakable, and a durability of zero is not a valid max damage.
	public static Item.Properties strayArmor(Item.Properties properties, ArmorType type) {
		return properties
			.attributes(STRAY_CLOTH.createAttributes(type))
			.enchantable(STRAY_CLOTH.enchantmentValue())
			.component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()).setEquipSound(STRAY_CLOTH.equipSound()).setAsset(STRAY_CLOTH.assetId()).build())
			.repairable(STRAY_CLOTH.repairIngredient());
	}
}
