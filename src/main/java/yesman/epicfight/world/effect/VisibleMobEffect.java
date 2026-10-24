package yesman.epicfight.world.effect;

import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;

public class VisibleMobEffect extends MobEffect {
	protected final Int2ObjectMap<Identifier> icons;
	protected final Function<MobEffectInstance, Integer> metadataGetter;
	
	public VisibleMobEffect(MobEffectCategory category, int color, Identifier textureLocation) {
		this(category, color, (effectInstance) -> 0, textureLocation);
	}
	
	public VisibleMobEffect(MobEffectCategory category, int color, Function<MobEffectInstance, Integer> metadataGetter, Identifier... textureLocations) {
		super(MobEffectCategory.BENEFICIAL, color);
		this.icons = new Int2ObjectOpenHashMap<>();
		this.metadataGetter = metadataGetter;
		
		for (int i = 0; i < textureLocations.length; i++) {
			this.icons.put(i, textureLocations[i]);
		}
	}
	
	@SuppressWarnings("deprecation")
	public Identifier getIcon(MobEffectInstance effectInstance) {
		return this.icons.get(this.metadataGetter.apply(effectInstance));
	}
}