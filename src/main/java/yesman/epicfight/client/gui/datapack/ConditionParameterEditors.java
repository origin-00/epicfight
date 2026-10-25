package yesman.epicfight.client.gui.datapack;

import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition.ParameterEditor;
import yesman.epicfight.data.conditions.entity.HealthPoint;
import yesman.epicfight.registry.EpicFightRegistries;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/// Builds the datapack editor widgets for the built-in conditions.
///
/// Conditions are loaded on the dedicated server too, so their `getAcceptingParameters` only delegate here:
/// keeping widget types out of the condition classes stops the verifier from loading client classes on the server.
public final class ConditionParameterEditors {
	private ConditionParameterEditors() {}

	public static List<ParameterEditor> customTag(Screen screen) {
		ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("tag"), null, null);
		return List.of(ParameterEditor.of((value) -> StringTag.valueOf(value.toString()), (tag) -> ParseUtil.nullOrToString(tag, ParseUtil::tagAsString), (AbstractWidget)editbox));
	}

	public static List<ParameterEditor> healthPoint(Screen screen) {
		ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("health"), null, null);
		AbstractWidget comboBox = new ComboBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, 4, Component.literal("comparator"), List.of(HealthPoint.Comparator.values()), ParseUtil::snakeToSpacedCamel, null);

		editbox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));

		return List.of(
			ParameterEditor.of((value) -> ParseUtil.parseOrGet(value.toString(), (v) -> DoubleTag.valueOf(Double.parseDouble(value.toString())), StringTag.valueOf("")), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, ParseUtil::tagAsString)), editbox),
			ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> ParseUtil.enumValueOfOrNull(HealthPoint.Comparator.class, ParseUtil.nullOrToString(tag, ParseUtil::tagAsString)), comboBox)
		);
	}

	public static List<ParameterEditor> offhandItemCategory(Screen screen) {
		AbstractWidget comboBox = new ComboBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, 4, Component.literal("category"), List.copyOf(WeaponCategory.ENUM_MANAGER.universalValues()), ParseUtil::snakeToSpacedCamel, null);
		return List.of(ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> WeaponCategory.ENUM_MANAGER.get(ParseUtil.nullOrToString(tag, ParseUtil::tagAsString)), comboBox));
	}

	public static List<ParameterEditor> playerName(Screen screen) {
		ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("identifier"), null, null);
		return List.of(ParameterEditor.of((name) -> StringTag.valueOf(name.toString()), (tag) -> ParseUtil.nullOrToString(tag, ParseUtil::tagAsString), editbox));
	}

	public static List<ParameterEditor> playerSkillActivated(Screen screen) {
		AbstractWidget popupBox = new PopupBox.RegistryPopupBox<>(screen, screen.getMinecraft().font, 0, 0, 0, 0, null, null, Component.literal("skill"), EpicFightRegistries.SKILL, null);
		return List.of(ParameterEditor.of(skill -> StringTag.valueOf(skill.toString()), tag -> EpicFightRegistries.SKILL.get(Identifier.parse(ParseUtil.nullOrToString(tag, ParseUtil::tagAsString))), popupBox));
	}

	public static List<ParameterEditor> randomChance(Screen screen) {
		ResizableEditBox editbox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("chance"), null, null);
		editbox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));

		return List.of(ParameterEditor.of((value) -> ParseUtil.parseOrGet(value.toString(), (v) -> DoubleTag.valueOf(Double.parseDouble(value.toString())), StringTag.valueOf("")), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, ParseUtil::tagAsString)), editbox));
	}

	/// Shared by the min/max range conditions (distance, angle).
	public static List<ParameterEditor> doubleRange(Screen screen) {
		ResizableEditBox minEditBox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("min"), null, null);
		ResizableEditBox maxEditBox = new ResizableEditBox(screen.getMinecraft().font, 0, 0, 0, 0, Component.literal("max"), null, null);
		minEditBox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
		maxEditBox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
		Function<Object, Tag> doubleParser = (value) -> DoubleTag.valueOf(Double.valueOf(value.toString()));
		Function<Tag, Object> doubleGetter = (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, ParseUtil::tagAsString));

		return List.of(ParameterEditor.of(doubleParser, doubleGetter, minEditBox), ParameterEditor.of(doubleParser, doubleGetter, maxEditBox));
	}
}
