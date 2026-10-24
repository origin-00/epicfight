package yesman.epicfight.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.Weapon;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.List;

public class WeaponItem extends Item {
    public WeaponItem(Item.Properties properties) {
        super(properties);
    }
    
    /// Gives an item the common sword behavior that used to be overridden from `SwordItem`:
    /// one durability point per hit, and no block breaking in creative mode.
    ///
    /// Since 1.21.2 both are data components rather than overridable methods. Items built through
    /// [Item.Properties#sword] already have them; this is for weapons that are not.
    public static Item.Properties swordBehavior(Item.Properties properties) {
        return properties
            .component(DataComponents.WEAPON, new Weapon(1))
            .component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 2, false));
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return itemAbility == ItemAbilities.SWORD_SWEEP;
    }
}
