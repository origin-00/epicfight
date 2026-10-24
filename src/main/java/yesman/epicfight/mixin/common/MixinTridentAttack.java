package yesman.epicfight.mixin.common;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(TridentItem.class)
public abstract class MixinTridentAttack {
    /// Tridents are spawned through Projectile#spawnProjectileFromRotation since 1.21.2.
    @Inject(method = "releaseUsing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectileFromRotation(Lnet/minecraft/world/entity/projectile/Projectile$ProjectileFactory;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;FFF)Lnet/minecraft/world/entity/projectile/Projectile;", shift = At.Shift.AFTER))
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft, CallbackInfoReturnable<Boolean> ci) {
        EpicFightCapabilities.getUnparameterizedEntityPatch(entityLiving, LivingEntityPatch.class).ifPresent(LivingEntityPatch::playShootingAnimation);
    }
}
