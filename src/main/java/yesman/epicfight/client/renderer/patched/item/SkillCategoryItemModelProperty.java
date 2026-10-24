package yesman.epicfight.client.renderer.patched.item;

import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import yesman.epicfight.EpicFight;
import yesman.epicfight.world.item.SkillBookItem;

/// `epicfight:skill_category`: the book icon of the skill category a skill book holds, for the
/// `minecraft:select` item model in `assets/epicfight/items/skillbook.json`.
///
/// Replaces the numeric `epicfight:skill` item property and the baked overrides built from it:
/// item properties and model overrides were replaced by item model definitions in 1.21.4. A skill
/// category added by another mod is selected by its book icon id.
public record SkillCategoryItemModelProperty() implements SelectItemModelProperty<Identifier> {
	public static final Identifier ID = EpicFight.identifier("skill_category");
	public static final SelectItemModelProperty.Type<SkillCategoryItemModelProperty, Identifier> TYPE = SelectItemModelProperty.Type.create(
		MapCodec.unit(new SkillCategoryItemModelProperty()), Identifier.CODEC
	);
	
	@Override
	public @Nullable Identifier get(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
		return SkillBookItem.getContainSkill(itemStack).map(skill -> skill.value().getCategory().bookIcon()).orElse(null);
	}
	
	@Override
	public SelectItemModelProperty.Type<SkillCategoryItemModelProperty, Identifier> type() {
		return TYPE;
	}
	
	@Override
	public Codec<Identifier> valueCodec() {
		return Identifier.CODEC;
	}
}
