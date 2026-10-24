package yesman.epicfight.api.ex_cap.modules.core.data;

import net.minecraft.resources.Identifier;

/**
 * Represents a single entry in the ExCap data file.
 * @param id The ID of the entry.
 * @param data The data associated with the entry.
 * @deprecated For Removal. All this has been delegated to the WeaponModifier.
 */
@Deprecated(forRemoval = true)
public record ExCapDataEntry(Identifier id, ExCapData.Builder data) { }