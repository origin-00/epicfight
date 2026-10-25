package yesman.epicfight.api.event.impl;

import java.util.function.Predicate;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Lets another mod own the damage judgement of some entities.
///
/// Epic Fight normally judges guard, parry and dodge in [LivingEntityPatch#tryHurt], reached only
/// from vanilla's hurt path ([VanillaEntityEventHooks#onDamageIncomes]). A mod with its own damage
/// pipeline (one that never calls `hurtServer` for some blows) would see a raised guard stop
/// vanilla swings and nothing else. Such a mod claims those entities here, so Epic Fight stops
/// judging them on the vanilla path, and asks [#judge] itself at every point its own pipeline
/// decides a blow, which runs the exact same judgement once per blow on both paths.
///
/// Both hooks default to "not claimed" and "unchanged", so nothing changes until a mod sets them.
public final class ExternalDamageAuthority {
	/// Parameters of Epic Fight's skills that an external authority may tune per entity.
	public enum Parameter {
		/// Ticks after a guard press in which a guarded blow counts as a parry ([yesman.epicfight.skill.guard.ParryingSkill])
		PARRY_WINDOW,
		/// Ticks after a parry attempt before another guard press opens a fresh parry window
		PARRY_COOLDOWN
	}

	@FunctionalInterface
	public interface ParameterTuner {
		/// Returns the value `entity` should use for `parameter`, given Epic Fight's own `value`
		int tune(LivingEntity entity, Parameter parameter, int value);
	}

	/// The outcome of one judgement, with how Epic Fight reached it.
	///
	/// @param result      what [LivingEntityPatch#tryHurt] returns for the blow
	/// @param canceled    whether a [yesman.epicfight.api.event.EpicFightEventHooks.Entity#TAKE_DAMAGE_INCOME] listener (guard, parry, stance) caught the blow
	/// @param parried     whether that catch was a parry
	/// @param eventResult the result the catching listener reported ([AttackResult.ResultType#BLOCKED] for a guard)
	public record Judgement(AttackResult result, boolean canceled, boolean parried, AttackResult.ResultType eventResult) {
		public static Judgement unjudged(float amount) {
			return new Judgement(AttackResult.success(amount), false, false, AttackResult.ResultType.SUCCESS);
		}
	}

	private static volatile Predicate<LivingEntity> judgedExternally = entity -> false;
	private static volatile ParameterTuner parameterTuner = (entity, parameter, value) -> value;

	private ExternalDamageAuthority() {}

	/// Claims every entity `predicate` accepts: Epic Fight no longer judges their incoming damage
	/// on the vanilla path, and the claiming mod calls [#judge] instead.
	public static void setJudgedExternally(Predicate<LivingEntity> predicate) {
		judgedExternally = predicate == null ? entity -> false : predicate;
	}

	public static boolean isJudgedExternally(LivingEntity entity) {
		return judgedExternally.test(entity);
	}

	public static void setParameterTuner(ParameterTuner tuner) {
		parameterTuner = tuner == null ? (entity, parameter, value) -> value : tuner;
	}

	public static int tune(LivingEntity entity, Parameter parameter, int value) {
		return parameterTuner.tune(entity, parameter, value);
	}

	/// Runs Epic Fight's judgement of one incoming blow, exactly as the vanilla path would, and
	/// records the result on the attacker's patch like [VanillaEntityEventHooks#onDamageIncomes]
	/// does. Server side only. An entity with no patch is never caught.
	public static Judgement judge(LivingEntity entity, DamageSource damageSource, float amount) {
		LivingEntityPatch<?> entitypatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);

		if (entitypatch == null) {
			return Judgement.unjudged(amount);
		}

		Judgement judgement = entitypatch.judgeHurt(damageSource, amount);

		EpicFightCapabilities.getUnparameterizedEntityPatch(damageSource.getEntity(), LivingEntityPatch.class).ifPresent(attackerentitypatch -> {
			attackerentitypatch.setLastAttackResult(judgement.result());
		});

		return judgement;
	}
}
