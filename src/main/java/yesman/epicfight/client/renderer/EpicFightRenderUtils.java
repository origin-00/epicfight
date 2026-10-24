package yesman.epicfight.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.EpicFight;

public final class EpicFightRenderUtils {
	private EpicFightRenderUtils() {}
	
	/// The living entity a render state was extracted from.
	///
	/// Vanilla render states deliberately hold no entity reference since 1.21.2, but Epic Fight
	/// renders from the entity patch (armature, animator, decorations). It is attached when the
	/// state is extracted, through NeoForge's render state modifiers.
	public static final ContextKey<LivingEntity> RENDERED_ENTITY = new ContextKey<> (EpicFight.identifier("rendered_entity"));
	
	/// Whether vanilla renders this entity upside down (named "Dinnerbone" or "Grumm"). Became an
	/// instance method of LivingEntityRenderer in 1.21.2; the rule itself is unchanged.
	public static boolean isUpsideDown(LivingEntity entity) {
		if (entity.getCustomName() == null) {
			return false;
		}
		
		String name = entity.getCustomName().getString();
		return "Dinnerbone".equals(name) || "Grumm".equals(name);
	}
	
	/// Whether entity hitboxes are shown (F3 + B). Since 1.21.9 this is a debug screen entry.
	public static boolean shouldRenderHitBoxes() {
		return Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES);
	}
}
