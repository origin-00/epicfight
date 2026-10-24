package yesman.epicfight.compat;

import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.EpicFight;

// List of mods with custom compatibility modules.
// Only includes mods requiring manual registration via ICompatModule.
// Mods with official API entry-points (e.g., Shoulder Surfing, Controlify, JEI, KubeJS) are excluded.
//
// 26.1.2 port: every module is null until the dependency has a 26.1.2 release and its module is ported
// back into the build (see the compat exclusion in build.gradle.kts).
public enum MinecraftMod {
    VAMPIRISM("vampirism", false, null),
    WEREWOLVES("werewolves", false, null),
    CURIOS_API("curios", false, null),
    GECKO_LIB("geckolib", true, null),
    AZURE_LIB("azurelib", true, null),
    AZURE_LIB_ARMOR("azurelibarmor", true, null),
    FIRST_PERSON("firstperson", true, null),
    SKIN_LAYERS_3D("skinlayers3d", true, null),
    IRIS("iris", true, null),
    PLAYER_ANIMATOR("playeranimator", true, null),
    BETTER_THIRD_PERSON("betterthirdperson", true, null),
    PLAYER_REVIVE("playerrevive", true, null),
    WILDFIRES_GENDER_MOD("wildfire_gender", true, null),
    SIMPLY_TOOLTIPS("simplytooltips", true, null);

    private final @NotNull String modId;
    private final boolean isClientOnly;
    private final @Nullable Class<? extends ICompatModule> compatibilityModule;

    MinecraftMod(@NotNull String modId, boolean isClientOnly, @Nullable Class<? extends ICompatModule> compatibilityModule) {
        this.modId = modId;
        this.isClientOnly = isClientOnly;
        this.compatibilityModule = compatibilityModule;
    }

    public @NotNull String getModId() {
        return modId;
    }

    public boolean isClientOnly() {
        return isClientOnly;
    }

    public String versionString() {
        return ModList.get().getModFileById(modId).versionString();
    }

    // https://semver.org
    public enum VersionComponent {
        MAJOR(0), MINOR(1), PATCH(2);

        final int index;

        VersionComponent(int index) {
            this.index = index;
        }
    }

    /// Returns the parsed component version value or `null` if parsing fails.
    ///
    /// Example values for version `"2.4.9"`:
    ///
    /// - [VersionComponent#MAJOR] -> `2`
    /// - [VersionComponent#MINOR] -> `4`
    /// - [VersionComponent#PATCH] -> `9`
    ///
    /// Usage example:
    ///
    /// ```java
    /// MinecraftMod mod = MinecraftMod.AZURE_LIB; // Version "3.4.11"
    ///
    /// Integer major = mod.getVersionComponent(VersionComponent.MAJOR); // 3
    /// Integer minor = mod.getVersionComponent(VersionComponent.MINOR); // 4
    /// Integer patch = mod.getVersionComponent(VersionComponent.PATCH); // 11
    ///```
    public @Nullable Integer getVersionComponent(@NotNull VersionComponent component) {
        final String version = versionString();

        try {
            final String[] parts = version.split("\\.");
            return Integer.parseInt(parts[component.index]);
        } catch (Exception e) {
            EpicFight.LOGGER.error("Failed to parse the '{}' mod version '{}': {}", name(), version, e.toString());
            return null;
        }
    }

    public @Nullable Class<? extends ICompatModule> getCompatibilityModule() {
        return compatibilityModule;
    }
}
