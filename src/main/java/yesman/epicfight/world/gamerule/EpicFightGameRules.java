package yesman.epicfight.world.gamerule;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.level.GameRuleChangedEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.config.CommonConfig;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeGamerule;
import yesman.epicfight.skill.SkillSlots;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class EpicFightGameRules {
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> GLOBAL_STUN = create(
			  "globalStun"
			, GameRuleCategory.MOBS
			, configBuilder -> configBuilder.define("default_gamerule.globalStun", true)
			, RuleType.BOOLEAN
			, false
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> KEEP_SKILLS = create(
			  "keepSkills"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.define("default_gamerule.keepSkills", true)
			, RuleType.BOOLEAN
			, false
	);
	
	/// When false, skill books can't be read and no longer drop as loot, so skills come only from
	/// what a server grants (e.g. a stat or progression system driving the skill slots).
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> SKILL_BOOKS = create(
			  "skillBooks"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.define("default_gamerule.skillBooks", true)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> HAS_FALL_ANIMATION = create(
			  "hasFallAnimation"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.define("default_gamerule.hasFallAnimation", true)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> DISABLE_ENTITY_UI = create(
			  "disableEntityUI"
			, GameRuleCategory.MISC
			, configBuilder -> configBuilder.define("default_gamerule.disapleEntityUI", false)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> CAN_SWITCH_PLAYER_MODE = create(
			  "canSwitchPlayerMode"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.define("default_gamerule.canSwitchPlayerMode", true)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> STIFF_COMBO_ATTACKS = create(
			  "stiffComboAttacks"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.define("default_gamerule.stiffComboAttacks", true)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> NO_MOBS_IN_BOSSFIGHT = create(
			  "noMobsInBossfight"
			, GameRuleCategory.SPAWNING
			, configBuilder -> configBuilder.define("default_gamerule.noMobsInBossfight", true)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Integer, ModConfigSpec.IntValue> INITIAL_PLAYER_MODE = create(
			  "initialMode"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.comment("0 = vanilla, 1 = epicfight").defineInRange("default_gamerule.initialMode", 1, 0, 1)
			, RuleType.INTEGER
			, true
	);
	
	public static final ConfigurableGameRule<Integer, ModConfigSpec.IntValue> WEIGHT_PENALTY = EpicFightGameRules.create(
			  "weightPenalty"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.defineInRange("default_gamerule.weightPenalty", 100, 0, 100)
			, RuleType.INTEGER
			, true
	);
	
	public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> EPIC_DROP = EpicFightGameRules.create(
			  "epicDrop"
			, GameRuleCategory.DROPS
			, configBuilder -> configBuilder.define("default_gamerule.epicDrop", false)
			, RuleType.BOOLEAN
			, true
	);
	
	public static final ConfigurableGameRule<Integer, ModConfigSpec.IntValue> SKILL_REPLACE_COOLDOWN = EpicFightGameRules.create(
			  "skillReplaceCooldown"
			, GameRuleCategory.PLAYER
			, configBuilder -> configBuilder.defineInRange("default_gamerule.skillReplaceCooldown", 6000, 0, Integer.MAX_VALUE)
			, RuleType.INTEGER
			, true
	);

    public static final ConfigurableGameRule<Boolean, ModConfigSpec.BooleanValue> ALLOW_VANILLA_MELEE = EpicFightGameRules.create(
        "allowVanillaMelee"
        , GameRuleCategory.PLAYER
        , configBuilder -> configBuilder.define("default_gamerule.allow_vanilla_melee", true)
        , RuleType.BOOLEAN
        , true
    );

    /// Maximum number of passive skills a player may have equipped at once. Default 3 preserves
    /// the legacy hard cap. Upper bound is [SkillSlots#CORE_PASSIVE_SLOT_COUNT] because that's how
    /// many passive `SkillSlots` enum constants this mod actually declares; raising past that has
    /// no effect.
    public static final ConfigurableGameRule<Integer, ModConfigSpec.IntValue> MAX_PASSIVE_SKILLS = EpicFightGameRules.create(
          "maxPassiveSkills"
        , GameRuleCategory.PLAYER
        , configBuilder -> configBuilder.defineInRange("default_gamerule.maxPassiveSkills", 3, 0, SkillSlots.CORE_PASSIVE_SLOT_COUNT)
        , RuleType.INTEGER
        , true
    );

	public static final Map<String, ConfigurableGameRule<?, ?>> GAME_RULES = ImmutableMap.<String, ConfigurableGameRule<?, ?>>builder()
			.put("globalStun", GLOBAL_STUN)
			.put("keepSkills", KEEP_SKILLS)
			.put("hasFallAnimation", HAS_FALL_ANIMATION)
			.put("disableEntityUI", DISABLE_ENTITY_UI)
			.put("canSwitchPlayerMode", CAN_SWITCH_PLAYER_MODE)
			.put("stiffComboAttacks", STIFF_COMBO_ATTACKS)
			.put("noMobsInBossfight", NO_MOBS_IN_BOSSFIGHT)
			.put("initialMode", INITIAL_PLAYER_MODE)
			.put("weightPenalty", WEIGHT_PENALTY)
			.put("epicDrop", EPIC_DROP)
			.put("skillReplaceCooldown", SKILL_REPLACE_COOLDOWN)
            .put("allowVanillaMelee", ALLOW_VANILLA_MELEE)
            .put("maxPassiveSkills", MAX_PASSIVE_SKILLS)
			.build();
	
	/// Since 26.1 game rules are entries of a registry; the default value of each comes from
	/// [CommonConfig], so rules are created lazily when the registry event fires.
	public static final DeferredRegister<GameRule<?>> REGISTRY = DeferredRegister.create(Registries.GAME_RULE, EpicFightMod.MODID);

	static {
		GAME_RULES.values().forEach(ConfigurableGameRule::register);
	}

	/// Values of synchronized rules as last sent by the server. Since 26.1 a client level has no
	/// [GameRules] of its own, so the client reads rules from here instead.
	private static final Map<ConfigurableGameRule<?, ?>, Object> CLIENT_VALUES = new ConcurrentHashMap<> ();

	/// Sends the new value of a synchronized rule to every player when it changes.
	public static void onGameRuleChanged(GameRuleChangedEvent event) {
		for (ConfigurableGameRule<?, ?> gamerule : GAME_RULES.values()) {
			if (gamerule.synchronize && gamerule.gameRule.isBound() && event.getGameRule() == gamerule.gameRule.get()) {
				EpicFightNetworkManager.sendToAll(new SPChangeGamerule(new KeyValuePair(gamerule.unchecked(), event.getNewValue())));
			}
		}
	}

	public static void clearClientValues() {
		CLIENT_VALUES.clear();
	}

	public static <Type, Config extends ModConfigSpec.ConfigValue<Type>> ConfigurableGameRule<Type, Config> create(
		  String ruleName
		, GameRuleCategory ruleCategory
		, Function<ModConfigSpec.Builder, Config> configDefinition
		, RuleType<Type> ruleType
		, boolean synchronize
	) {
		return new ConfigurableGameRule<> (ruleName, ruleCategory, configDefinition, ruleType, synchronize);
	}

	public static class ConfigurableGameRule<Type, Config extends ModConfigSpec.ConfigValue<Type>> {
		final String ruleName;
		final GameRuleCategory ruleCategory;
		final Function<ModConfigSpec.Builder, Config> configDefinition;
		final RuleType<Type> ruleType;
		final boolean synchronize;

		Config configValueHolder;
		DeferredHolder<GameRule<?>, GameRule<Type>> gameRule;

		private ConfigurableGameRule(
			  String ruleName
			, GameRuleCategory ruleCategory
			, Function<ModConfigSpec.Builder, Config> configDefinition
			, RuleType<Type> ruleType
			, boolean synchronize
		) {
			this.ruleName = ruleName;
			this.ruleCategory = ruleCategory;
			this.configDefinition = configDefinition;
			this.ruleType = ruleType;
			this.synchronize = synchronize;
		}

		private void register() {
			this.gameRule = REGISTRY.register(toRegistryPath(this.ruleName), () -> this.ruleType.factory.apply(this.ruleCategory, this.configuredDefault()));
		}

		/// The config may not be loaded yet when the registry event fires; fall back to its default then.
		private Type configuredDefault() {
			try {
				return this.configValueHolder.get();
			} catch (IllegalStateException e) {
				return this.configValueHolder.getDefault();
			}
		}

		@SuppressWarnings("unchecked")
		ConfigurableGameRule<Object, ?> unchecked() {
			return (ConfigurableGameRule<Object, ?>)this;
		}

		public boolean shouldSync() {
			return this.synchronize;
		}

		public SPChangeGamerule getSyncPacket(ServerPlayer player) {
			return new SPChangeGamerule(new EpicFightGameRules.KeyValuePair(this.unchecked(), this.getRuleValue(player.level())));
		}

		public void defineConfig(ModConfigSpec.Builder configBuilder) {
			this.configValueHolder = this.configDefinition.apply(configBuilder);
		}

		public String getRuleName() {
			return this.ruleName;
		}

		public RuleType<Type> getRuleType() {
			return this.ruleType;
		}

		public GameRule<Type> getGameRule() {
			return this.gameRule.get();
		}

		public Config getConfigHolder() {
			return this.configValueHolder;
		}

		@SuppressWarnings("unchecked")
		public Type getRuleValue(Level level) {
			if (level instanceof ServerLevel serverLevel) {
				return serverLevel.getGameRules().get(this.gameRule.get());
			}

			return (Type)CLIENT_VALUES.getOrDefault(this, this.gameRule.get().defaultValue());
		}

		public void setRuleValue(Level level, Type value) {
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.getGameRules().set(this.gameRule.get(), value, serverLevel.getServer());
			} else {
				CLIENT_VALUES.put(this, value);
			}
		}
	}

	/// `snake_case` registry path from the legacy `camelCase` rule name, as identifiers cannot
	/// contain upper-case letters (e.g. `globalStun` becomes `epicfight:global_stun`).
	static String toRegistryPath(String ruleName) {
		return ruleName.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
	}

	public static record RuleType<Type> (
		  BiFunction<GameRuleCategory, Type, GameRule<Type>> factory
		, StreamCodec<ByteBuf, Type> codec
	) {
		private static final RuleType<Boolean> BOOLEAN = new RuleType<> (
			  (category, defaultValue) -> new GameRule<> (
				  category
				, GameRuleType.BOOL
				, BoolArgumentType.bool()
				, GameRuleTypeVisitor::visitBoolean
				, Codec.BOOL
				, b -> b ? 1 : 0
				, defaultValue
				, FeatureFlagSet.of()
			  )
			, ByteBufCodecs.BOOL
		);

		private static final RuleType<Integer> INTEGER = new RuleType<> (
			  (category, defaultValue) -> new GameRule<> (
				  category
				, GameRuleType.INT
				, IntegerArgumentType.integer(0)
				, GameRuleTypeVisitor::visitInteger
				, Codec.intRange(0, Integer.MAX_VALUE)
				, i -> i
				, defaultValue
				, FeatureFlagSet.of()
			  )
			, ByteBufCodecs.INT
		);
	}

	public static record KeyValuePair(ConfigurableGameRule<Object, ?> gamerule, Object value) {
	}
}
