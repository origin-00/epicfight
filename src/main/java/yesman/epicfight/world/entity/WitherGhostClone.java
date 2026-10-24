package yesman.epicfight.world.entity;

import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import yesman.epicfight.registry.entries.EpicFightAttributes;
import yesman.epicfight.registry.entries.EpicFightEntityTypes;

public class WitherGhostClone extends Mob {
	public WitherGhostClone(EntityType<? extends Mob> entityType, Level level) {
		super(entityType, level);
		this.setNoGravity(true);
		this.noPhysics = true;
	}
	
	public WitherGhostClone(ServerLevel level, Vec3 position, LivingEntity target) {
		this(EpicFightEntityTypes.WITHER_GHOST_CLONE.get(), level);
		this.setPos(position);
		this.lookAt(Anchor.FEET, target.position());
		this.setTarget(target);
	}
	
	@Override
	public boolean hurtServer(ServerLevel level, DamageSource damagesource, float damage) {
		if (!damagesource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			return false;
		}
		
		return super.hurtServer(level, damagesource, damage);
	}
	
	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes().add(EpicFightAttributes.WEIGHT).add(EpicFightAttributes.ARMOR_NEGATION).add(EpicFightAttributes.IMPACT).add(EpicFightAttributes.MAX_STRIKES).add(Attributes.ATTACK_DAMAGE);
	}
	
	@Override
	protected void customServerAiStep(ServerLevel level) {
		if (this.tickCount >= 40) {
			this.remove(RemovalReason.DISCARDED);
		}
	}
}