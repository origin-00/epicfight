package yesman.epicfight.client.gui;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.KeyEvent;

/// Inputs for widgets driven by code rather than by the player.
public final class GuiInputs {
	private GuiInputs() {}

	/// Passed to `AbstractButton#onPress`, which takes the triggering input since 1.21.9, when a
	/// button is pressed programmatically. Equivalent to pressing enter with no modifiers.
	public static final InputWithModifiers PROGRAMMATIC = new KeyEvent(InputConstants.KEY_RETURN, 0, 0);
}
