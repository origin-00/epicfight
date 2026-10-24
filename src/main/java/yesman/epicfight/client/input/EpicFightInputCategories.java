package yesman.epicfight.client.input;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.KeyMapping;
import yesman.epicfight.EpicFight;

/// Key mapping categories. Since 1.21.9 a category is a registered [KeyMapping.Category] whose
/// label is translated from `key.category.<namespace>.<path>`.
@ApiStatus.Internal
public final class EpicFightInputCategories {
    private EpicFightInputCategories() {
    }

    public static final KeyMapping.Category COMBAT = new KeyMapping.Category(EpicFight.identifier("combat"));
    public static final KeyMapping.Category GUI = new KeyMapping.Category(EpicFight.identifier("gui"));
    public static final KeyMapping.Category SYSTEM = new KeyMapping.Category(EpicFight.identifier("system"));
    public static final KeyMapping.Category CAMERA = new KeyMapping.Category(EpicFight.identifier("camera"));

    /// Registered in RegisterKeyMappingsEvent, in display order.
    public static final List<KeyMapping.Category> ALL = List.of(COMBAT, GUI, CAMERA, SYSTEM);
}
