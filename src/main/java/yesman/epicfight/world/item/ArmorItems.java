package yesman.epicfight.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.Nullable;

/// Armor queries that used to go through `ArmorItem`.
///
/// Since 1.21.2 armor is a plain [Item] with an [Equippable] component and armor attribute
/// modifiers, so the slot and defense value are read back from those components. The defense
/// is the same number the old `ArmorMaterial#defense()` map supplied for that piece.
public final class ArmorItems {
    private ArmorItems() {}

    /// The armor type of a wearable armor piece, or `null` for anything else (including elytra
    /// and carved pumpkins, which are equippable but give no armor).
    public static @Nullable ArmorType getArmorType(Item item) {
        Equippable equippable = item.components().get(DataComponents.EQUIPPABLE);

        if (equippable == null || equippable.slot().getType() != EquipmentSlot.Type.HUMANOID_ARMOR || getDefense(item) <= 0.0D) {
            return null;
        }

        return switch (equippable.slot()) {
            case HEAD -> ArmorType.HELMET;
            case CHEST -> ArmorType.CHESTPLATE;
            case LEGS -> ArmorType.LEGGINGS;
            case FEET -> ArmorType.BOOTS;
            default -> null;
        };
    }

    public static boolean isArmor(Item item) {
        return getArmorType(item) != null;
    }

    /// Sum of the flat armor modifiers the item applies.
    public static double getDefense(Item item) {
        ItemAttributeModifiers modifiers = item.components().getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        double defense = 0.0D;

        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ARMOR) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                defense += entry.modifier().amount();
            }
        }

        return defense;
    }
}
