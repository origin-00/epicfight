package yesman.epicfight.world.item;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import yesman.epicfight.generated.LangKeys;

import java.util.List;
import java.util.function.Consumer;

public class UchigatanaItem extends WeaponItem {
	public static ItemAttributeModifiers createUchigatanaAttributes() {
		return TieredWeaponItem.createAttributes(6.0F, -2.0F);
	}
	
	/// Enchantability and the iron-bars repair material used to be method overrides; since 1.21.2
	/// they are item properties.
	public static Item.Properties properties(Item.Properties properties) {
		return properties
			.component(DataComponents.WEAPON, new Weapon(1))
			.component(DataComponents.TOOL, createToolProperties())
			.enchantable(22)
			.repairable(Items.IRON_BARS)
			.attributes(createUchigatanaAttributes());
	}
	
	public UchigatanaItem(Item.Properties properties) {
		super(properties);
	}
	
	public static Tool createToolProperties() {
		HolderGetter<Block> blocks = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
		
        return new Tool(
        	List.of(
        		Tool.Rule.minesAndDrops(HolderSet.direct(Blocks.COBWEB.builtInRegistryHolder()), 15.0F),
        		Tool.Rule.overrideSpeed(blocks.getOrThrow(BlockTags.SWORD_EFFICIENT), 1.5F)
        	),
        	1.0F,
        	2,
        	false
        );
    }
    
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		tooltipComponents.accept(Component.literal(""));
		tooltipComponents.accept(Component.translatable(LangKeys.ITEM_UCHIGATANA_TOOLTIP));
	}
}
