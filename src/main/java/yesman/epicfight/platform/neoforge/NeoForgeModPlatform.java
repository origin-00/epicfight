package yesman.epicfight.platform.neoforge;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.platform.ModPlatform;

public final class NeoForgeModPlatform implements ModPlatform {
    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLEnvironment.isProduction();
    }

    @Override
    public boolean isModLoaded(@NotNull final String id) {
        return ModList.get().isLoaded(id);
    }
}
