package yesman.epicfight.api.event.types.player;

import yesman.epicfight.api.event.CancelableEvent;
import yesman.epicfight.api.event.LivingEntityPatchEvent;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class ComboAttackEvent extends LivingEntityPatchEvent implements CancelableEvent {
    private final net.minecraft.nbt.CompoundTag arguments;
    private yesman.epicfight.api.animation.AnimationManager.AnimationAccessor<? extends yesman.epicfight.api.animation.types.AttackAnimation> motionOverride;

	public ComboAttackEvent(ServerPlayerPatch playerPatch) {
        this(playerPatch, new net.minecraft.nbt.CompoundTag());
	}

	public ComboAttackEvent(ServerPlayerPatch playerPatch, net.minecraft.nbt.CompoundTag arguments) {
        super(playerPatch);
        this.arguments = arguments == null ? new net.minecraft.nbt.CompoundTag() : arguments;
	}

    /// What the client wrote through [yesman.epicfight.skill.common.ComboAttacks#ARGUMENT_WRITERS].
    public net.minecraft.nbt.CompoundTag getArguments() {
        return this.arguments;
    }

    /// Plays this attack instead of the combo's next one. The combo counter is left as it was.
    public void setMotionOverride(yesman.epicfight.api.animation.AnimationManager.AnimationAccessor<? extends yesman.epicfight.api.animation.types.AttackAnimation> motion) {
        this.motionOverride = motion;
    }

    public yesman.epicfight.api.animation.AnimationManager.AnimationAccessor<? extends yesman.epicfight.api.animation.types.AttackAnimation> getMotionOverride() {
        return this.motionOverride;
    }

    public ServerPlayerPatch getPlayerPatch() {
        return (ServerPlayerPatch)this.getEntityPatch();
    }
}