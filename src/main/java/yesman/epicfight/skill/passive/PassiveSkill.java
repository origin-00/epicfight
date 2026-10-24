package yesman.epicfight.skill.passive;

import net.minecraft.client.renderer.RenderPipelines;
import yesman.epicfight.client.gui.GuiColors;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.compress.utils.Lists;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;

import java.util.Collections;
import java.util.function.Function;

public abstract class PassiveSkill extends Skill {
	public static SkillBuilder<?> createPassiveBuilder(Function<SkillBuilder<?>, ? extends PassiveSkill> constructor) {
		return new SkillBuilder<>(constructor).setCategory(SkillCategories.PASSIVE).setResource(Resource.NONE);
	}
	
	@SuppressWarnings("rawtypes")
	public PassiveSkill(SkillBuilder<? extends SkillBuilder> builder) {
		super(builder);
	}
	
	@Override @ClientOnly
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphicsExtractor guiGraphics, float x, float y, float partialTick) {
		guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.getSkillTexture(), (int)x, (int)y, 0, 0, 24, 24, 1, 1, 1, 1);
		String remainTime = String.format("%.0f", container.getMaxResource() - container.getResource());
		guiGraphics.text(gui.getFont(), remainTime, (int)(x + 12 - 4 * remainTime.length()), (int)((y+6)), GuiColors.text(16777215), true);
	}

	@Override
	public Component getTranslatedTooltip(ItemStack itemStack, CapabilityItem itemCap, PlayerPatch<?> playerPatch) {
		return Component.translatable(this.getTranslationKey() + ".tooltip", getTooltipArgsOfScreen(Lists.newArrayList()).toArray(new Object[0]));
	}
}