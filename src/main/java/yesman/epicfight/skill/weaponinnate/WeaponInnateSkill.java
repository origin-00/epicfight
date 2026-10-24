package yesman.epicfight.skill.weaponinnate;

import net.minecraft.resources.Identifier;
import yesman.epicfight.client.gui.render.TexturedFanRenderState;
import net.minecraft.util.ARGB;
import yesman.epicfight.client.gui.GuiColors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import yesman.epicfight.api.animation.property.AnimationProperty.AttackPhaseProperty;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.api.utils.math.Vec2f;
import yesman.epicfight.api.utils.side.ClientOnly;
import yesman.epicfight.client.gui.BattleModeGui;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.damagesource.StunType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public abstract class WeaponInnateSkill extends Skill {
	@SuppressWarnings("unchecked")
	public static class Builder<B extends WeaponInnateSkill.Builder<B>> extends SkillBuilder<B> {
		private final ImmutableList.Builder<Map<AttackPhaseProperty<?>, Object>> propertiesBuilder = ImmutableList.builder();
		private Map<AttackPhaseProperty<?>, Object> lastPropertiesMap;
		
		public Builder(Function<B, ? extends Skill> constructor) {
			super(constructor);
		}
		
		public B newProperty() {
			this.lastPropertiesMap = new HashMap<>();
			this.propertiesBuilder.add(this.lastPropertiesMap);
			return (B)this;
		}
		
		public <T> B addProperty(AttackPhaseProperty<T> propertyKey, T value) {
			this.lastPropertiesMap.put(propertyKey, value);
			return (B)this;
		}
	}
	
	public static <B extends WeaponInnateSkill.Builder<B>> B createWeaponInnateBuilder(Function<B, ? extends WeaponInnateSkill> constructor) {
		return new WeaponInnateSkill.Builder<>(constructor).setCategory(SkillCategories.WEAPON_INNATE).setResource(Resource.WEAPON_CHARGE);
	}
	
	protected final List<Map<AttackPhaseProperty<?>, Object>> properties;
	
	public WeaponInnateSkill(WeaponInnateSkill.Builder<?> builder) {
		super(builder);
		
		this.properties = builder.propertiesBuilder.build();
	}
	
	@Override
	public boolean canExecute(SkillContainer container) {
		// Read the active combat hand so an offhand-only carrier (mirror mode) still passes the
		// "is this skill the cap's innate?" check. Hardcoding mainhand here was the reason
		// long-press attacks did nothing on offhand: the client-side gate failed before the cast
		// request was ever sent to the server.
		ItemStack itemstack = container.getExecutor().getOriginal().getItemInHand(container.getExecutor().getPrimaryHand());

		return super.canExecute(container)
				&& EpicFightCapabilities.getItemStackCapability(itemstack).getInnateSkill(container.getExecutor(), itemstack) == this
				&& container.getExecutor().getOriginal().getVehicle() == null
				&& (
					!this.isActivated(container) || this.activateType == ActivateType.TOGGLE
				);
	}
	
	@Override
	public List<Component> getTooltipOnItem(ItemStack itemstack, CapabilityItem cap, PlayerPatch<?> playerCap) {
		List<Component> list = Lists.newArrayList();
		String traslatableText = this.getTranslationKey();
		
		list.add(
			Component
				.translatable(traslatableText)
				.withStyle(ChatFormatting.WHITE)
				.append(
					Component
						.literal(String.format("[%.0f]", this.consumption))
						.withStyle(ChatFormatting.AQUA)
				)
		);
		
		list.add(
			Component
				.translatable(traslatableText + ".tooltip")
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		
		return list;
	}

	public List<Component> getSimplyTooltips(ItemStack itemStack, CapabilityItem itemCap, PlayerPatch<?> playerPatch) {
		List<Component> list = Lists.newArrayList();
		String translatableText = this.getTranslationKey();
		list.add(Component.translatable(translatableText).withStyle(ChatFormatting.WHITE));
		list.add(Component.empty());
		list.add(Component.translatable("text.epicfight.consumption").append(Component.literal(String.format(" %.0f", this.consumption)).withStyle(ChatFormatting.AQUA)));
		list.add(Component.empty());
		list.add(getTranslatedTooltip(itemStack, itemCap, playerPatch));

		return list;
	}


	
	protected void generateTooltipforPhase(List<Component> list, ItemStack itemstack, CapabilityItem itemcap, PlayerPatch<?> playerpatch, Map<AttackPhaseProperty<?>, Object> propertyMap, String title) {
		double weaponBaseDamage = playerpatch.getWeaponAttribute(Attributes.ATTACK_DAMAGE, itemstack);
		double armorNegation = playerpatch.getWeaponAttribute(EpicFightAttributes.ARMOR_NEGATION, itemstack);
		double impact = playerpatch.getWeaponAttribute(EpicFightAttributes.IMPACT, itemstack);
		double maxStrikes = playerpatch.getWeaponAttribute(EpicFightAttributes.MAX_STRIKES, itemstack);
		ValueModifier.ResultCalculator damageModifier = ValueModifier.calculator();
		ValueModifier.ResultCalculator armorNegationModifier = ValueModifier.calculator();
		ValueModifier.ResultCalculator impactModifier = ValueModifier.calculator();
		ValueModifier.ResultCalculator maxStrikesModifier = ValueModifier.calculator();
		
		getProperty(AttackPhaseProperty.DAMAGE_MODIFIER, propertyMap).ifPresent(damageModifier::attach);
		getProperty(AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, propertyMap).ifPresent(armorNegationModifier::attach);
		getProperty(AttackPhaseProperty.IMPACT_MODIFIER, propertyMap).ifPresent(impactModifier::attach);
		getProperty(AttackPhaseProperty.MAX_STRIKES_MODIFIER, propertyMap).ifPresent(maxStrikesModifier::attach);
		
		final double fBaseDamage = weaponBaseDamage;
		weaponBaseDamage = damageModifier.getResult(playerpatch.getModifiedBaseDamage((float)weaponBaseDamage));
		armorNegation = armorNegationModifier.getResult((float)armorNegation);
		impact = impactModifier.getResult((float)impact);
		maxStrikes = maxStrikesModifier.getResult((float)maxStrikes);
		
		list.add(
			Component
				.literal(title)
				.withStyle(ChatFormatting.UNDERLINE)
				.withStyle(ChatFormatting.GRAY)
		);
		
		MutableComponent damageComponent =
			Component
				.translatable(
					"damage_source.epicfight.damage",
					Component
						.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(weaponBaseDamage))
						.withStyle(ChatFormatting.RED)
				)
				.withStyle(ChatFormatting.DARK_GRAY);
		
		getProperty(AttackPhaseProperty.EXTRA_DAMAGE, propertyMap).ifPresent(extraDamageSet -> {
			extraDamageSet.forEach(extraDamage -> {
				extraDamage.setTooltips(playerpatch.getLevel(), itemstack, damageComponent, fBaseDamage);
			});
		});
		
		list.add(damageComponent);
		
		if (armorNegation != 0.0D) {
			list.add(
				Component
					.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(armorNegation) + "% ")
					.withStyle(ChatFormatting.GOLD)
					.append(
						Component
							.translatable(EpicFightAttributes.ARMOR_NEGATION.get().getDescriptionId())
							.withStyle(ChatFormatting.DARK_GRAY)
					)
			);
		}
		
		if (impact != 0.0D) {
			list.add(
				Component
					.translatable(
						EpicFightAttributes.IMPACT.get().getDescriptionId() + ".value",
						Component
							.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(impact))
							.withStyle(ChatFormatting.AQUA)
					)
					.withStyle(ChatFormatting.DARK_GRAY)
			);
		}
		
		list.add(
			Component
				.translatable(
					EpicFightAttributes.MAX_STRIKES.get().getDescriptionId() + ".value",
					Component
						.literal(ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(maxStrikes))
						.withStyle(ChatFormatting.WHITE)
				)
				.withStyle(ChatFormatting.DARK_GRAY)
		);
		
		Optional<StunType> stunOption = getProperty(AttackPhaseProperty.STUN_TYPE, propertyMap);
		
		stunOption.ifPresentOrElse(stunType -> {
			list.add(
				Component
					.translatable(stunType.toString())
					.withStyle(ChatFormatting.DARK_GRAY)
			);
		}, () -> {
			list.add(
				Component
					.translatable(StunType.SHORT.toString())
					.withStyle(ChatFormatting.DARK_GRAY)
			);
		});
	}
	
	@SuppressWarnings("unchecked")
	protected static <V> Optional<V> getProperty(AttackPhaseProperty<V> propertyKey, Map<AttackPhaseProperty<?>, Object> map) {
		return Optional.ofNullable((V)map.get(propertyKey));
	}
	
	public WeaponInnateSkill newProperty() {
		this.properties.add(new HashMap<> ());
		
		return this;
	}
	
	public <T> WeaponInnateSkill addProperty(AttackPhaseProperty<T> propertyKey, T object) {
		this.properties.get(properties.size() - 1).put(propertyKey, object);
		
		return this;
	}
	
	private static final Vec2f[] CLOCK_POS = {
		new Vec2f(0.5F, 0.5F),
		new Vec2f(0.5F, 0.0F),
		new Vec2f(0.0F, 0.0F),
		new Vec2f(0.0F, 1.0F),
		new Vec2f(1.0F, 1.0F),
		new Vec2f(1.0F, 0.0F)
	};
	
	@Override @ClientOnly
	public boolean shouldDraw(SkillContainer container) {
		return true;
	}
	
	@Override @ClientOnly
	public void drawOnGui(BattleModeGui gui, SkillContainer container, GuiGraphicsExtractor guiGraphics, float x, float y, float partialTick) {
		boolean creative = container.getExecutor().getOriginal().isCreative();
		boolean fullstack = creative || container.isFull();
		boolean canUse = !container.isDisabled() && container.getSkill().checkExecuteCondition(container);
		
		float cooldownRatio = (fullstack || container.isActivated()) ? 1.0F : container.getResource(partialTick);
		int vertexNum = 0;
		float iconSize = 32.0F;
		float bottom = y + iconSize;
		float right = x + iconSize;
		float middle = x + iconSize * 0.5F;
		float lastVertexX = 0;
		float lastVertexY = 0;
		float lastTexX = 0;
		float lastTexY = 0;
		
		if (cooldownRatio < 0.125F) {
			vertexNum = 6;
			lastTexX = cooldownRatio / 0.25F;
			lastTexY = 0.0F;
			lastVertexX = middle + iconSize * lastTexX;
			lastVertexY = y;
			lastTexX += 0.5F;
		} else if (cooldownRatio < 0.375F) {
			vertexNum = 5;
			lastTexX = 1.0F;
			lastTexY = (cooldownRatio - 0.125F) / 0.25F;
			lastVertexX = right;
			lastVertexY = y + iconSize * lastTexY;
		} else if (cooldownRatio < 0.625F) {
			vertexNum = 4;
			lastTexX = (cooldownRatio - 0.375F) / 0.25F;
			lastTexY = 1.0F;
			lastVertexX = right - iconSize * lastTexX;
			lastVertexY = bottom;
			lastTexX = 1.0F - lastTexX;
		} else if (cooldownRatio < 0.875F) {
			vertexNum = 3;
			lastTexX = 0.0F;
			lastTexY = (cooldownRatio - 0.625F) / 0.25F;
			lastVertexX = x;
			lastVertexY = bottom - iconSize * lastTexY;
			lastTexY = 1.0F - lastTexY;
		} else {
			vertexNum = 2;
			lastTexX = (cooldownRatio - 0.875F) / 0.25F;
			lastTexY = 0.0F;
			lastVertexX = x + iconSize * lastTexX;
			lastVertexY = y;
		}
		
		Identifier skillTexture = container.getSkill().getSkillTexture();
		int readyColor;
		
		if (canUse) {
			readyColor = container.getStack() > 0 ? ARGB.colorFromFloat(0.8F, 0.0F, 0.64F, 0.72F) : ARGB.colorFromFloat(0.6F, 0.0F, 0.5F, 0.5F);
		} else {
			readyColor = ARGB.colorFromFloat(0.6F, 0.5F, 0.5F, 0.5F);
		}
		
		// The elapsed part of the cooldown: a fan from the icon center clockwise to the current angle.
		float[] elapsedVertices = new float[(vertexNum + 1) * 2];
		float[] elapsedUvs = new float[(vertexNum + 1) * 2];
		
		for (int j = 0; j < vertexNum; j++) {
			elapsedVertices[j * 2] = x + iconSize * CLOCK_POS[j].x;
			elapsedVertices[j * 2 + 1] = y + iconSize * CLOCK_POS[j].y;
			elapsedUvs[j * 2] = CLOCK_POS[j].x;
			elapsedUvs[j * 2 + 1] = CLOCK_POS[j].y;
		}
		
		elapsedVertices[vertexNum * 2] = lastVertexX;
		elapsedVertices[vertexNum * 2 + 1] = lastVertexY;
		elapsedUvs[vertexNum * 2] = lastTexX;
		elapsedUvs[vertexNum * 2 + 1] = lastTexY;
		TexturedFanRenderState.submit(guiGraphics, skillTexture, elapsedVertices, elapsedUvs, readyColor);
		
		// The remaining part, from the current angle back to twelve o'clock.
		int remaining = CLOCK_POS.length - vertexNum;
		float[] remainingVertices = new float[(2 + remaining + 1) * 2];
		float[] remainingUvs = new float[(2 + remaining + 1) * 2];
		int k = 0;
		
		for (int j = 0; j < 2; j++, k++) {
			remainingVertices[k * 2] = x + iconSize * CLOCK_POS[j].x;
			remainingVertices[k * 2 + 1] = y + iconSize * CLOCK_POS[j].y;
			remainingUvs[k * 2] = CLOCK_POS[j].x;
			remainingUvs[k * 2 + 1] = CLOCK_POS[j].y;
		}
		
		for (int j = CLOCK_POS.length - 1; j >= vertexNum; j--, k++) {
			remainingVertices[k * 2] = x + iconSize * CLOCK_POS[j].x;
			remainingVertices[k * 2 + 1] = y + iconSize * CLOCK_POS[j].y;
			remainingUvs[k * 2] = CLOCK_POS[j].x;
			remainingUvs[k * 2 + 1] = CLOCK_POS[j].y;
		}
		
		remainingVertices[k * 2] = lastVertexX;
		remainingVertices[k * 2 + 1] = lastVertexY;
		remainingUvs[k * 2] = lastTexX;
		remainingUvs[k * 2 + 1] = lastTexY;
		TexturedFanRenderState.submit(guiGraphics, skillTexture, remainingVertices, remainingUvs, canUse ? ARGB.colorFromFloat(1.0F, 0.08F, 0.79F, 0.95F) : -1);
        
        if (container.isActivated() && (container.getSkill().getActivateType() == ActivateType.DURATION || container.getSkill().getActivateType() == ActivateType.DURATION_INFINITE)) {
			String s = String.format("%.0f", container.getRemainDuration() / 20.0F);
			int stringWidth = (gui.getFont().width(s) - 6) / 3;
			guiGraphics.text(gui.getFont(), s, (int)(x + 13 - stringWidth), (int)(y + 13), GuiColors.text(16777215), true);
		} else if (!fullstack) {
			String s = String.valueOf((int)(cooldownRatio * 100.0F));
			int stringWidth = (gui.getFont().width(s) - 6) / 3;
			guiGraphics.text(gui.getFont(), s, (int)(x + 13 - stringWidth), (int)(y + 13), GuiColors.text(16777215), true);
		}
		
		if (container.getSkill().getMaxStack() > 1) {
			String s = String.valueOf(container.getStack());
			int stringWidth = (gui.getFont().width(s) - 6) / 3;
			guiGraphics.text(gui.getFont(), s, (int)(x + 25 - stringWidth), (int)(y + 22), GuiColors.text(16777215), true);
		}
		
	}
}
