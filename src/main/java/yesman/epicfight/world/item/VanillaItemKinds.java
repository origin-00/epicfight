package yesman.epicfight.world.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.level.block.Block;

import java.util.Set;

/// Classifies items whose dedicated classes were removed in 1.21.2.
///
/// `SwordItem`, `PickaxeItem` and `DiggerItem` are gone: such items are now plain [Item]s
/// configured with a [Tool] component, so they are recognized from that component instead.
/// Checks use the component's block-tag *keys*, never tag contents, so they are valid before
/// tags are bound (e.g. while data reload listeners run).
public final class VanillaItemKinds {
    private VanillaItemKinds() {}

    private static final Set<TagKey<Block>> DIGGER_TAGS = Set.of(
        BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_SHOVEL, BlockTags.MINEABLE_WITH_HOE
    );

    /// A sword is a weapon whose tool component refuses to break blocks in creative, which is
    /// what [net.minecraft.world.item.ToolMaterial#applySwordProperties] sets up.
    public static boolean isSword(Item item) {
        if (item instanceof MaceItem || item instanceof TridentItem) {
            return false;
        }

        Tool tool = item.components().get(DataComponents.TOOL);
        return tool != null && !tool.canDestroyBlocksInCreative() && item.components().has(DataComponents.WEAPON);
    }

    public static boolean isPickaxe(Item item) {
        return minesTag(item, Set.of(BlockTags.MINEABLE_WITH_PICKAXE));
    }

    /// Pickaxes, axes, shovels and hoes: the old `DiggerItem` family.
    public static boolean isDigger(Item item) {
        return minesTag(item, DIGGER_TAGS);
    }

    private static boolean minesTag(Item item, Set<TagKey<Block>> tags) {
        Tool tool = item.components().get(DataComponents.TOOL);
        return tool != null && tool.rules().stream().anyMatch(rule -> rule.blocks().unwrapKey().filter(tags::contains).isPresent());
    }
}
