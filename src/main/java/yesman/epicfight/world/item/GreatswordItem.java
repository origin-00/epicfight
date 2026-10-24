package yesman.epicfight.world.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.world.capabilities.item.WeaponCapabilityPresets;

public class GreatswordItem extends TieredWeaponItem {
	public static final Identifier GREATSWORD_MOVEMENT_SPEED_PENALTY_ID = Identifier.withDefaultNamespace("greatsword_movement_speed_penalty");
	
	public static ItemAttributeModifiers createGreatswordAttributes(ToolMaterial material) {
        return createGreatswordAttributes(11.0F + material.attackDamageBonus(), -2.85F + WeaponCapabilityPresets.vanillaTierToLevel(material) * -0.05F);
    }
	
	public static ItemAttributeModifiers createGreatswordAttributes(float attackDamage, float attackSpeed) {
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
            .add(
            	  Attributes.MOVEMENT_SPEED
	            , new AttributeModifier(GREATSWORD_MOVEMENT_SPEED_PENALTY_ID, -0.02D, AttributeModifier.Operation.ADD_VALUE)
	            , EquipmentSlotGroup.MAINHAND
            )
            .build();
    }
	
	public GreatswordItem(ToolMaterial material, Item.Properties properties) {
		super(material, properties);
	}
}