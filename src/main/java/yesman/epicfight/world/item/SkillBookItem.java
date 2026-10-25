package yesman.epicfight.world.item;

import yesman.epicfight.world.gamerule.EpicFightGameRules;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import yesman.epicfight.registry.entries.EpicFightDataComponentTypes;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

public class SkillBookItem extends Item {
	public static void setContainingSkill(Holder<Skill> skill, ItemStack stack) {
		stack.applyComponents(DataComponentPatch.builder().set(EpicFightDataComponentTypes.SKILL.get(), skill).build());
	}
	
	public static Optional<Holder<Skill>> getContainSkill(ItemStack stack) {
		return Optional.ofNullable(stack.get(EpicFightDataComponentTypes.SKILL.get()));
	}
	
	public SkillBookItem(Properties properties) {
		super(properties);
	}
	
	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.has(EpicFightDataComponentTypes.SKILL.get());
	}
	
	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
		if (stack.has(EpicFightDataComponentTypes.SKILL.get())) {
			Holder<Skill> skill = stack.get(EpicFightDataComponentTypes.SKILL.get());
			Identifier registryName = skill.getKey().identifier();
			tooltipComponents.accept(Component.translatable(String.format("skill.%s.%s", registryName.getNamespace(), registryName.getPath())).withStyle(ChatFormatting.DARK_GRAY));
		}
	}
	
	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack itemstack = player.getItemInHand(hand);
		
		if (!EpicFightGameRules.SKILL_BOOKS.getRuleValue(world)) {
			return InteractionResult.FAIL;
		}
		
		EpicFightCapabilities.getUnparameterizedEntityPatch(player, PlayerPatch.class).ifPresent(playerpatch -> {
			playerpatch.openSkillBook(itemstack, hand);
		});
		
		player.awardStat(Stats.ITEM_USED.get(this));
		
		return InteractionResult.PASS;
	}
}