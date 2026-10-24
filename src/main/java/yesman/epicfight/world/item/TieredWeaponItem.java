package yesman.epicfight.world.item;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

/// A weapon built from a [ToolMaterial], replacing the `SwordItem`/`Tier` pair removed in 1.21.2.
///
/// The sword components (durability, repair material, enchantability, cobweb mining) come from
/// [Item.Properties#sword]; [#properties] then replaces the vanilla sword attributes with the
/// weapon-specific ones, so callers only pass the material and the attribute modifiers.
public abstract class TieredWeaponItem extends WeaponItem {
    public static ItemAttributeModifiers createAttributes(ToolMaterial material, float attackDamage, float attackSpeed, float attackSpeedModifier) {
        return createAttributes(attackDamage + material.attackDamageBonus(), attackSpeed + WeaponCapabilityPresets.vanillaTierToLevel(material) * attackSpeedModifier);
    }
	
    public static ItemAttributeModifiers createAttributes(float attackDamage, float attackSpeed) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ATTACK_DAMAGE
                , new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (double)attackDamage, AttributeModifier.Operation.ADD_VALUE)
                , EquipmentSlotGroup.MAINHAND
            )
            .add(
                  Attributes.ATTACK_SPEED
                , new AttributeModifier(BASE_ATTACK_SPEED_ID, (double)attackSpeed, AttributeModifier.Operation.ADD_VALUE)
                , EquipmentSlotGroup.MAINHAND
            )
            .build();
    }
    
    /// Applies the sword components of `material`, then overrides the attributes with `attributes`.
    public static Item.Properties properties(Item.Properties properties, ToolMaterial material, ItemAttributeModifiers attributes) {
        return properties.sword(material, 0.0F, 0.0F).attributes(attributes);
    }
    
    private final ToolMaterial material;
    
	public TieredWeaponItem(ToolMaterial material, Item.Properties properties) {
		super(properties);
		this.material = material;
	}
	
	public ToolMaterial getMaterial() {
		return this.material;
	}
}
