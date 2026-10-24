package yesman.epicfight.world.capabilities.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import yesman.epicfight.api.ex_cap.managers.ExCapManager;
import yesman.epicfight.registry.entries.EpicFightSounds;
import yesman.epicfight.world.item.ToolMaterials;

public abstract class WeaponCapabilityPresets {
	public static int vanillaTierToLevel(ToolMaterial material) {
		if (material == ToolMaterial.WOOD || material == ToolMaterial.GOLD) return 0;
		// Copper (added in 1.21.9) sits between stone and iron and shares stone's level.
		if (material == ToolMaterial.STONE || material == ToolMaterial.COPPER) return 1;
		if (material == ToolMaterial.IRON) return 2;
		if (material == ToolMaterial.DIAMOND) return 3;
		if (material == ToolMaterial.NETHERITE) return 4;
		
		double sqrt = Math.sqrt(material.durability());

		// Custom tier mapping
		return sqrt < 10.0D ? 0 : (int)Math.round(sqrt / 10.0D);
	}

    public static CapabilityItem.Builder<?> registerPreset(CapabilityItem.Builder<?> builder, Item item)
    {
        if (builder instanceof WeaponCapability.Builder weaponBuilder)
        {
            WeaponCapability.Builder copy = weaponBuilder.copy();
            handleItemOverrides(copy, item);
            ExCapManager.retrieveExCapData(builder.identifier).forEach(
                    data -> data.build().apply(copy)
            );
            return copy;
        }

        return CapabilityItem.builder();
    }

    private static void handleItemOverrides(WeaponCapability.Builder builder, Item item)
    {
        ToolMaterial material = ToolMaterials.of(item);
        
        if (material != null) {
            int tierLevel = vanillaTierToLevel(material);
            builder.modifyTierAttributes(tierLevel);
            if (material == ToolMaterial.WOOD)
            {
                builder.hitSound(EpicFightSounds.BLUNT_HIT);
            }
            if (material == ToolMaterial.STONE)
            {
                builder.hitSound(EpicFightSounds.BLUNT_HIT_HARD);
            }
        }
        builder.handleOverrides(item);
    }
}
