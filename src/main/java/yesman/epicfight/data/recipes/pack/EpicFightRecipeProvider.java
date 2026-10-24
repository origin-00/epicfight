package yesman.epicfight.data.recipes.pack;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import yesman.epicfight.registry.entries.EpicFightItems;

public class EpicFightRecipeProvider extends RecipeProvider {
	public EpicFightRecipeProvider(Provider registries, RecipeOutput output) {
		super(registries, output);
	}
	
	public static class Runner extends RecipeProvider.Runner {
		public Runner(PackOutput output, CompletableFuture<Provider> registries) {
			super(output, registries);
		}
		
		@Override
		protected RecipeProvider createRecipeProvider(Provider registries, RecipeOutput output) {
			return new EpicFightRecipeProvider(registries, output);
		}
		
		@Override
		public String getName() {
			return "Epic Fight Recipes";
		}
	}
	
	@Override
    protected void buildRecipes() {
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.DIAMOND_DAGGER.get())
			.pattern(" X")
			.pattern("# ")
			.define('X', Items.DIAMOND)
			.define('#', Items.STICK)
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.DIAMOND_GREATSWORD.get())
			.pattern(" XX")
			.pattern("XXX")
			.pattern("#X ")
			.define('X', Items.DIAMOND)
			.define('#', Items.STICK)
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.DIAMOND_LONGSWORD.get())
			.pattern("  X")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', Items.DIAMOND)
			.define('#', Items.DIAMOND_SWORD)
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.DIAMOND_SPEAR.get())
			.pattern("  X")
			.pattern(" # ")
			.pattern("#  ")
			.define('X', Items.DIAMOND_SWORD)
			.define('#', Items.STICK)
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.DIAMOND_TACHI.get())
			.pattern(" X ")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', Items.DIAMOND)
			.define('#', Items.DIAMOND_SWORD)
			.unlockedBy("has_diamond", has(Items.DIAMOND))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.GLOVE.get())
			.pattern("XX")
			.pattern("##")
			.define('X', Items.IRON_NUGGET)
			.define('#', Items.LEATHER)
			.unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.GOLDEN_DAGGER.get())
			.pattern(" X")
			.pattern("# ")
			.define('X', Items.GOLD_INGOT)
			.define('#', Items.STICK)
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.GOLDEN_GREATSWORD.get())
			.pattern(" XX")
			.pattern("XXX")
			.pattern("#X ")
			.define('X', Items.GOLD_INGOT)
			.define('#', Items.STICK)
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.GOLDEN_LONGSWORD.get())
			.pattern("  X")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', Items.GOLD_INGOT)
			.define('#', Items.GOLDEN_SWORD)
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.GOLDEN_SPEAR.get())
			.pattern("  X")
			.pattern(" # ")
			.pattern("#  ")
			.define('X', Items.GOLDEN_SWORD)
			.define('#', Items.STICK)
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.GOLDEN_TACHI.get())
			.pattern(" X ")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', Items.GOLD_INGOT)
			.define('#', Items.GOLDEN_SWORD)
			.unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.IRON_DAGGER.get())
			.pattern(" X")
			.pattern("# ")
			.define('X', Items.IRON_INGOT)
			.define('#', Items.STICK)
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.IRON_GREATSWORD.get())
			.pattern(" XX")
			.pattern("XXX")
			.pattern("#X ")
			.define('X', Items.IRON_INGOT)
			.define('#', Items.STICK)
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.IRON_LONGSWORD.get())
			.pattern("  X")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', Items.IRON_INGOT)
			.define('#', Items.IRON_SWORD)
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.IRON_SPEAR.get())
			.pattern("  X")
			.pattern(" # ")
			.pattern("#  ")
			.define('X', Items.IRON_SWORD)
			.define('#', Items.STICK)
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.IRON_TACHI.get())
			.pattern(" X ")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', Items.IRON_INGOT)
			.define('#', Items.IRON_SWORD)
			.unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
			.save(this.output)
			;
		
		netheriteSmithing(EpicFightItems.DIAMOND_DAGGER.get(), RecipeCategory.COMBAT, EpicFightItems.NETHERITE_DAGGER.get());
		netheriteSmithing(EpicFightItems.DIAMOND_GREATSWORD.get(), RecipeCategory.COMBAT, EpicFightItems.NETHERITE_GREATSWORD.get());
		netheriteSmithing(EpicFightItems.DIAMOND_LONGSWORD.get(), RecipeCategory.COMBAT, EpicFightItems.NETHERITE_LONGSWORD.get());
		netheriteSmithing(EpicFightItems.DIAMOND_SPEAR.get(), RecipeCategory.COMBAT, EpicFightItems.NETHERITE_SPEAR.get());
		netheriteSmithing(EpicFightItems.DIAMOND_TACHI.get(), RecipeCategory.COMBAT, EpicFightItems.NETHERITE_TACHI.get());
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.STONE_DAGGER.get())
			.pattern(" X")
			.pattern("# ")
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.define('#', Items.STICK)
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.STONE_GREATSWORD.get())
			.pattern(" XX")
			.pattern("XXX")
			.pattern("#X ")
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.define('#', Items.STICK)
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.STONE_LONGSWORD.get())
			.pattern("  X")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.define('#', Items.STONE_SWORD)
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.STONE_SPEAR.get())
			.pattern("  X")
			.pattern(" # ")
			.pattern("#  ")
			.define('X', Items.STONE_SWORD)
			.define('#', Items.STICK)
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.STONE_TACHI.get())
			.pattern(" X ")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', ItemTags.STONE_TOOL_MATERIALS)
			.define('#', Items.STONE_SWORD)
			.unlockedBy("has_cobblestone", has(ItemTags.STONE_TOOL_MATERIALS))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.WOODEN_DAGGER.get())
			.pattern(" X")
			.pattern("# ")
			.define('X', ItemTags.PLANKS)
			.define('#', Items.STICK)
			.unlockedBy("has_stick", has(Items.STICK))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.WOODEN_GREATSWORD.get())
			.pattern(" XX")
			.pattern("XXX")
			.pattern("#X ")
			.define('X', ItemTags.PLANKS)
			.define('#', Items.STICK)
			.unlockedBy("has_stick", has(Items.STICK))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.WOODEN_LONGSWORD.get())
			.pattern("  X")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', ItemTags.PLANKS)
			.define('#', Items.WOODEN_SWORD)
			.unlockedBy("has_stick", has(Items.STICK))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.WOODEN_SPEAR.get())
			.pattern("  X")
			.pattern(" # ")
			.pattern("#  ")
			.define('X', Items.WOODEN_SWORD)
			.define('#', Items.STICK)
			.unlockedBy("has_stick", has(Items.STICK))
			.save(this.output)
			;
		
		ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, EpicFightItems.WOODEN_TACHI.get())
			.pattern(" X ")
			.pattern(" X ")
			.pattern("#  ")
			.define('X', ItemTags.PLANKS)
			.define('#', Items.WOODEN_SWORD)
			.unlockedBy("has_stick", has(Items.STICK))
			.save(this.output)
			;
	}
}
