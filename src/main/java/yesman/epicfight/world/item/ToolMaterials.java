package yesman.epicfight.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.Repairable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/// Resolves the [ToolMaterial] an item was built from.
///
/// Since 1.21.2, vanilla tools are plain [Item]s configured through components, so the old
/// `TieredItem#getTier()` no longer exists. Epic Fight items keep their material on
/// [TieredWeaponItem]; for anything else the material is recovered from the repair-material tag,
/// which every vanilla [ToolMaterial] sets through [Item.Properties#repairable].
public final class ToolMaterials {
    private ToolMaterials() {}

    public static final List<ToolMaterial> VANILLA = List.of(
        ToolMaterial.WOOD, ToolMaterial.STONE, ToolMaterial.COPPER, ToolMaterial.IRON,
        ToolMaterial.GOLD, ToolMaterial.DIAMOND, ToolMaterial.NETHERITE
    );

    public static @Nullable ToolMaterial of(ItemStack stack) {
        return of(stack.getItem());
    }

    public static @Nullable ToolMaterial of(Item item) {
        if (item instanceof TieredWeaponItem tieredWeaponItem) {
            return tieredWeaponItem.getMaterial();
        }

        // Only items that are actually tools or weapons count; a repairable armor piece shares the tag.
        if (!item.components().has(DataComponents.TOOL) && !item.components().has(DataComponents.WEAPON)) {
            return null;
        }

        Repairable repairable = item.components().get(DataComponents.REPAIRABLE);

        if (repairable == null) {
            return null;
        }

        return repairable.items().unwrapKey()
            .flatMap(tag -> VANILLA.stream().filter(material -> material.repairItems().equals(tag)).findFirst())
            .orElse(null);
    }
}
