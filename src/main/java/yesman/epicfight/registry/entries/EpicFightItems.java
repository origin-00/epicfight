package yesman.epicfight.registry.entries;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.world.item.DaggerItem;
import yesman.epicfight.world.item.GloveItem;
import yesman.epicfight.world.item.GreatswordItem;
import yesman.epicfight.world.item.LongswordItem;
import yesman.epicfight.world.item.SkillBookItem;
import yesman.epicfight.world.item.SpearItem;
import yesman.epicfight.world.item.TachiItem;
import yesman.epicfight.world.item.TieredWeaponItem;
import yesman.epicfight.world.item.UchigatanaItem;

public final class EpicFightItems {
	private EpicFightItems() {}
	
	// Since 1.21.2 an item needs its registry id in its properties before it is constructed;
	// DeferredRegister.Items#registerItem supplies it, which is why every entry builds its
	// properties from the one it is handed.
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(EpicFightMod.MODID);
	
	// Uchigatana & sheath
	public static final DeferredItem<UchigatanaItem> UCHIGATANA = REGISTRY.registerItem("uchigatana",
		properties -> new UchigatanaItem(UchigatanaItem.properties(properties.rarity(Rarity.RARE).durability(1625)))
	);
	public static final DeferredItem<Item> UCHIGATANA_SHEATH = REGISTRY.registerItem("uchigatana_sheath",
		properties -> new Item(properties.rarity(Rarity.EPIC))
	);
	
	// Greatsword variants
	public static final DeferredItem<GreatswordItem> WOODEN_GREATSWORD = REGISTRY.registerItem("wooden_greatsword",
		properties -> new GreatswordItem(ToolMaterial.WOOD, TieredWeaponItem.properties(properties, ToolMaterial.WOOD, GreatswordItem.createGreatswordAttributes(ToolMaterial.WOOD)))
	);
	public static final DeferredItem<GreatswordItem> STONE_GREATSWORD = REGISTRY.registerItem("stone_greatsword",
		properties -> new GreatswordItem(ToolMaterial.STONE, TieredWeaponItem.properties(properties, ToolMaterial.STONE, GreatswordItem.createGreatswordAttributes(ToolMaterial.STONE)))
	);
	public static final DeferredItem<GreatswordItem> IRON_GREATSWORD = REGISTRY.registerItem("iron_greatsword",
		properties -> new GreatswordItem(ToolMaterial.IRON, TieredWeaponItem.properties(properties, ToolMaterial.IRON, GreatswordItem.createGreatswordAttributes(ToolMaterial.IRON)))
	);
	public static final DeferredItem<GreatswordItem> GOLDEN_GREATSWORD = REGISTRY.registerItem("golden_greatsword",
		properties -> new GreatswordItem(ToolMaterial.GOLD, TieredWeaponItem.properties(properties, ToolMaterial.GOLD, GreatswordItem.createGreatswordAttributes(ToolMaterial.GOLD)))
	);
	public static final DeferredItem<GreatswordItem> DIAMOND_GREATSWORD = REGISTRY.registerItem("diamond_greatsword",
		properties -> new GreatswordItem(ToolMaterial.DIAMOND, TieredWeaponItem.properties(properties, ToolMaterial.DIAMOND, GreatswordItem.createGreatswordAttributes(ToolMaterial.DIAMOND)))
	);
	public static final DeferredItem<GreatswordItem> NETHERITE_GREATSWORD = REGISTRY.registerItem("netherite_greatsword",
		properties -> new GreatswordItem(ToolMaterial.NETHERITE, TieredWeaponItem.properties(properties.fireResistant(), ToolMaterial.NETHERITE, GreatswordItem.createGreatswordAttributes(ToolMaterial.NETHERITE)))
	);
	
	// Spear variants
	public static final DeferredItem<SpearItem> WOODEN_SPEAR = REGISTRY.registerItem("wooden_spear",
		properties -> new SpearItem(ToolMaterial.WOOD, TieredWeaponItem.properties(properties, ToolMaterial.WOOD, SpearItem.createSpearAttributes(ToolMaterial.WOOD)))
	);
	public static final DeferredItem<SpearItem> STONE_SPEAR = REGISTRY.registerItem("stone_spear",
		properties -> new SpearItem(ToolMaterial.STONE, TieredWeaponItem.properties(properties, ToolMaterial.STONE, SpearItem.createSpearAttributes(ToolMaterial.STONE)))
	);
	public static final DeferredItem<SpearItem> IRON_SPEAR = REGISTRY.registerItem("iron_spear",
		properties -> new SpearItem(ToolMaterial.IRON, TieredWeaponItem.properties(properties, ToolMaterial.IRON, SpearItem.createSpearAttributes(ToolMaterial.IRON)))
	);
	public static final DeferredItem<SpearItem> GOLDEN_SPEAR = REGISTRY.registerItem("golden_spear",
		properties -> new SpearItem(ToolMaterial.GOLD, TieredWeaponItem.properties(properties, ToolMaterial.GOLD, SpearItem.createSpearAttributes(ToolMaterial.GOLD)))
	);
	public static final DeferredItem<SpearItem> DIAMOND_SPEAR = REGISTRY.registerItem("diamond_spear",
		properties -> new SpearItem(ToolMaterial.DIAMOND, TieredWeaponItem.properties(properties, ToolMaterial.DIAMOND, SpearItem.createSpearAttributes(ToolMaterial.DIAMOND)))
	);
	public static final DeferredItem<SpearItem> NETHERITE_SPEAR = REGISTRY.registerItem("netherite_spear",
		properties -> new SpearItem(ToolMaterial.NETHERITE, TieredWeaponItem.properties(properties.fireResistant(), ToolMaterial.NETHERITE, SpearItem.createSpearAttributes(ToolMaterial.NETHERITE)))
	);
	
	// Tachi variants
	public static final DeferredItem<TachiItem> WOODEN_TACHI = REGISTRY.registerItem("wooden_tachi",
		properties -> new TachiItem(ToolMaterial.WOOD, TieredWeaponItem.properties(properties, ToolMaterial.WOOD, TachiItem.createTachiAttributes(ToolMaterial.WOOD)))
	);
	public static final DeferredItem<TachiItem> STONE_TACHI = REGISTRY.registerItem("stone_tachi",
		properties -> new TachiItem(ToolMaterial.STONE, TieredWeaponItem.properties(properties, ToolMaterial.STONE, TachiItem.createTachiAttributes(ToolMaterial.STONE)))
	);
	public static final DeferredItem<TachiItem> IRON_TACHI = REGISTRY.registerItem("iron_tachi",
		properties -> new TachiItem(ToolMaterial.IRON, TieredWeaponItem.properties(properties, ToolMaterial.IRON, TachiItem.createTachiAttributes(ToolMaterial.IRON)))
	);
	public static final DeferredItem<TachiItem> GOLDEN_TACHI = REGISTRY.registerItem("golden_tachi",
		properties -> new TachiItem(ToolMaterial.GOLD, TieredWeaponItem.properties(properties, ToolMaterial.GOLD, TachiItem.createTachiAttributes(ToolMaterial.GOLD)))
	);
	public static final DeferredItem<TachiItem> DIAMOND_TACHI = REGISTRY.registerItem("diamond_tachi",
		properties -> new TachiItem(ToolMaterial.DIAMOND, TieredWeaponItem.properties(properties, ToolMaterial.DIAMOND, TachiItem.createTachiAttributes(ToolMaterial.DIAMOND)))
	);
	public static final DeferredItem<TachiItem> NETHERITE_TACHI = REGISTRY.registerItem("netherite_tachi",
		properties -> new TachiItem(ToolMaterial.NETHERITE, TieredWeaponItem.properties(properties.fireResistant(), ToolMaterial.NETHERITE, TachiItem.createTachiAttributes(ToolMaterial.NETHERITE)))
	);
	
	// Longsword variants
	public static final DeferredItem<LongswordItem> WOODEN_LONGSWORD = REGISTRY.registerItem("wooden_longsword",
		properties -> new LongswordItem(ToolMaterial.WOOD, TieredWeaponItem.properties(properties, ToolMaterial.WOOD, LongswordItem.createLongswordAttributes(ToolMaterial.WOOD)))
	);
	public static final DeferredItem<LongswordItem> STONE_LONGSWORD = REGISTRY.registerItem("stone_longsword",
		properties -> new LongswordItem(ToolMaterial.STONE, TieredWeaponItem.properties(properties, ToolMaterial.STONE, LongswordItem.createLongswordAttributes(ToolMaterial.STONE)))
	);
	public static final DeferredItem<LongswordItem> IRON_LONGSWORD = REGISTRY.registerItem("iron_longsword",
		properties -> new LongswordItem(ToolMaterial.IRON, TieredWeaponItem.properties(properties, ToolMaterial.IRON, LongswordItem.createLongswordAttributes(ToolMaterial.IRON)))
	);
	public static final DeferredItem<LongswordItem> GOLDEN_LONGSWORD = REGISTRY.registerItem("golden_longsword",
		properties -> new LongswordItem(ToolMaterial.GOLD, TieredWeaponItem.properties(properties, ToolMaterial.GOLD, LongswordItem.createLongswordAttributes(ToolMaterial.GOLD)))
	);
	public static final DeferredItem<LongswordItem> DIAMOND_LONGSWORD = REGISTRY.registerItem("diamond_longsword",
		properties -> new LongswordItem(ToolMaterial.DIAMOND, TieredWeaponItem.properties(properties, ToolMaterial.DIAMOND, LongswordItem.createLongswordAttributes(ToolMaterial.DIAMOND)))
	);
	public static final DeferredItem<LongswordItem> NETHERITE_LONGSWORD = REGISTRY.registerItem("netherite_longsword",
		properties -> new LongswordItem(ToolMaterial.NETHERITE, TieredWeaponItem.properties(properties.fireResistant(), ToolMaterial.NETHERITE, LongswordItem.createLongswordAttributes(ToolMaterial.NETHERITE)))
	);
	
	// Dagger variants
	public static final DeferredItem<DaggerItem> WOODEN_DAGGER = REGISTRY.registerItem("wooden_dagger",
		properties -> new DaggerItem(ToolMaterial.WOOD, TieredWeaponItem.properties(properties, ToolMaterial.WOOD, DaggerItem.createDaggerAttributes(ToolMaterial.WOOD)))
	);
	public static final DeferredItem<DaggerItem> STONE_DAGGER = REGISTRY.registerItem("stone_dagger",
		properties -> new DaggerItem(ToolMaterial.STONE, TieredWeaponItem.properties(properties, ToolMaterial.STONE, DaggerItem.createDaggerAttributes(ToolMaterial.STONE)))
	);
	public static final DeferredItem<DaggerItem> IRON_DAGGER = REGISTRY.registerItem("iron_dagger",
		properties -> new DaggerItem(ToolMaterial.IRON, TieredWeaponItem.properties(properties, ToolMaterial.IRON, DaggerItem.createDaggerAttributes(ToolMaterial.IRON)))
	);
	public static final DeferredItem<DaggerItem> GOLDEN_DAGGER = REGISTRY.registerItem("golden_dagger",
		properties -> new DaggerItem(ToolMaterial.GOLD, TieredWeaponItem.properties(properties, ToolMaterial.GOLD, DaggerItem.createDaggerAttributes(ToolMaterial.GOLD)))
	);
	public static final DeferredItem<DaggerItem> DIAMOND_DAGGER = REGISTRY.registerItem("diamond_dagger",
		properties -> new DaggerItem(ToolMaterial.DIAMOND, TieredWeaponItem.properties(properties, ToolMaterial.DIAMOND, DaggerItem.createDaggerAttributes(ToolMaterial.DIAMOND)))
	);
	public static final DeferredItem<DaggerItem> NETHERITE_DAGGER = REGISTRY.registerItem("netherite_dagger",
		properties -> new DaggerItem(ToolMaterial.NETHERITE, TieredWeaponItem.properties(properties.fireResistant(), ToolMaterial.NETHERITE, DaggerItem.createDaggerAttributes(ToolMaterial.NETHERITE)))
	);
	
	// Weapons etc
	public static final DeferredItem<GloveItem> GLOVE = REGISTRY.registerItem("glove",
		properties -> new GloveItem(GloveItem.properties(properties.durability(1625).attributes(TieredWeaponItem.createAttributes(2.0F, 0.0F))))
	);
	public static final DeferredItem<Item> BOKKEN = REGISTRY.registerItem("bokken",
		properties -> new Item(properties.sword(ToolMaterial.WOOD, 3.0F, -2.4F))
	);
	
	// Stray armor
	public static final DeferredItem<Item> STRAY_HAT = REGISTRY.registerItem("stray_hat", properties -> new Item(EpicFightArmorMaterials.strayArmor(properties, ArmorType.HELMET)));
	public static final DeferredItem<Item> STRAY_ROBE = REGISTRY.registerItem("stray_robe", properties -> new Item(EpicFightArmorMaterials.strayArmor(properties, ArmorType.CHESTPLATE)));
	public static final DeferredItem<Item> STRAY_PANTS = REGISTRY.registerItem("stray_pants", properties -> new Item(EpicFightArmorMaterials.strayArmor(properties, ArmorType.LEGGINGS)));
	
	public static final DeferredItem<SkillBookItem> SKILLBOOK = REGISTRY.registerItem("skillbook", properties -> new SkillBookItem(properties.rarity(Rarity.RARE).stacksTo(1)));
}
