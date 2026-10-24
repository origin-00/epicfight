package yesman.epicfight.client.gui.datapack.screen;

import net.minecraft.client.input.MouseButtonEvent;
import yesman.epicfight.client.gui.GuiColors;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringUtil;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.main.EpicFightMod;

public class SelectFromRegistryScreen<T> extends Screen {
	private final RegistryList registryList;
	private final Screen parentScreen;
	private final Consumer<T> onPressRow;
	private final BiConsumer<String, T> onAccept;
	private final BiConsumer<String, T> onCancel;
	
	public SelectFromRegistryScreen(Screen parentScreen, Registry<T> registry, BiConsumer<String, T> onAccept, BiConsumer<String, T> onCancel, Predicate<T> filter) {
		this(parentScreen, registry, onAccept, onCancel, (select) -> {}, filter);
	}
	
	public SelectFromRegistryScreen(Screen parentScreen, Registry<T> registry, BiConsumer<String, T> onAccept, BiConsumer<String, T> onCancel, Consumer<T> onPressRow, Predicate<T> filter) {
		super(Component.translatable("gui.epicfight.select", ParseUtil.snakeToSpacedCamel(registry.key().identifier().getPath())));
		
		this.parentScreen = parentScreen;
		
		final Map<Identifier, T> filteredItems = Maps.newHashMap();
		
		registry.listElements().map(Holder::value).filter(filter).forEach((value) -> filteredItems.put(registry.getKey(value), value));
		
		this.registryList = new RegistryList(parentScreen.getMinecraft(), this.width, this.height - 52, 36, 21, filteredItems);
		this.onPressRow = onPressRow;
		this.onAccept = onAccept;
		this.onCancel = onCancel;
	}
	
	public SelectFromRegistryScreen(Screen parentScreen, Set<Pair<Identifier, T>> entries, String title, BiConsumer<String, T> onAccept, BiConsumer<String, T> onCancel, Consumer<T> onPressRow, Predicate<T> filter) {
		super(Component.translatable("gui.epicfight.select", ParseUtil.snakeToSpacedCamel(title)));
		
		this.parentScreen = parentScreen;
		
		Map<Identifier, T> filteredItems = entries.stream().filter((entry) -> filter.test(entry.getSecond())).reduce(Maps.newHashMap(), (map, element) -> {
			map.put(element.getFirst(), element.getSecond());
			return map;
		}, (map1, map2) -> {
			map1.putAll(map2);
			return map1;
		});
		
		this.registryList = new RegistryList(parentScreen.getMinecraft(), this.width, this.height - 68, 36, 21, filteredItems);
		this.onPressRow = onPressRow;
		this.onAccept = onAccept;
		this.onCancel = onCancel;
	}
	
	@Override
	protected void init() {
		this.registryList.updateSizeAndPosition(this.width, this.height - 68, 36);
		
		EditBox editBox = new EditBox(this.minecraft.font, this.width / 2, 12, this.width / 2 - 12, 16, Component.literal(EpicFightMod.prefix("")));
		editBox.setResponder(this.registryList::applyFilter);
		
		this.addRenderableWidget(this.registryList);
		this.addRenderableWidget(editBox);
		
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_OK, (button$1) -> {
			if (this.registryList.getSelected() == null) {
				this.minecraft.setScreen(new MessageScreen<>("", "Select an item from the list", this, (button$2) -> {
					this.minecraft.setScreen(this);
				}, 180, 60));
			} else {
				try {
					this.onAccept.accept(this.registryList.getSelected().name, this.registryList.getSelected().item);
					this.minecraft.setScreen(this.parentScreen);
				} catch (Exception e) {
					this.minecraft.setScreen(new MessageScreen<>("", e.getMessage(), this.parentScreen, (button$2) -> this.minecraft.setScreen(this.parentScreen), 180, 70).autoCalculateHeight());
				}
			}
		}).pos(this.width / 2 - 162, this.height - 28).size(160, 21).build());
		
		this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
			this.onCancel.accept(StringUtils.EMPTY, null);
			this.minecraft.setScreen(this.parentScreen);
		}).pos(this.width / 2 + 2, this.height - 28).size(160, 21).build());
	}
	
	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		this.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
		guiGraphics.text(this.font, this.title, 20, 16, GuiColors.text(16777215));
		
        for (Renderable renderable : this.renderables) {
            renderable.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        }
	}
	
	@Override
	public void onClose() {
		this.minecraft.setScreen(this.parentScreen);
	}
	
	class RegistryList extends ObjectSelectionList<RegistryList.RegistryEntry> {
		private final Map<Identifier, T> registry;
		
		public RegistryList(Minecraft minecraft, int width, int height, int y, int defaultEntryHeight, Map<Identifier, T> registry) {
			super(minecraft, width, height, y, defaultEntryHeight);
			
			this.registry = registry;
			registry.entrySet().stream().sorted((entry1, entry2) -> entry1.getKey().toString().compareTo(entry2.getKey().toString())).forEach((entry) -> this.addEntry(new RegistryEntry(entry.getValue(), entry.getKey().toString())));
		}
		
		@Override
		public void setSelected(@Nullable RegistryEntry selEntry) {
			SelectFromRegistryScreen.this.onPressRow.accept(selEntry.item);
			super.setSelected(selEntry);
		}
		
		@Override
		public int getRowWidth() {
			return this.width;
		}
		
		@Override
		protected int scrollBarX() {
			return this.getX() - 6;
		}
		
		public void applyFilter(String keyward) {
			this.setScrollAmount(0.0D);
			this.children().clear();
			
			this.registry.entrySet().stream()
				.sorted((entry1, entry2) -> entry1.getKey().toString().compareTo(entry2.getKey().toString()))
				.filter((entry) -> StringUtil.isNullOrEmpty(keyward) ? true : entry.getKey().toString().contains(keyward))
				.map((entry) -> new RegistryEntry(entry.getValue(), entry.getKey().toString()))
				.forEach(this::addEntry);
		}
		
		class RegistryEntry extends ObjectSelectionList.Entry<RegistryList.RegistryEntry> {
			private final T item;
			private final String name;
			
			public RegistryEntry(T item, String name) {
				this.item = item;
				this.name = name;
			}
			
			@Override
			public void extractContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
				int top = this.getY();
				int left = this.getX();
				int width = this.getWidth();
				int height = this.getContentHeight();
				guiGraphics.text(SelectFromRegistryScreen.this.minecraft.font, this.name, left + 25, top + 5, GuiColors.text(16777215), false);
			}
			
			@Override
			public Component getNarration() {
				return Component.translatable("narrator.select");
			}
			
			@Override
			public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
				double mouseX = event.x();
				double mouseY = event.y();
				int button = event.button();
				if (button == 0) {
					if (RegistryList.this.getSelected() == this) {
						try {
							SelectFromRegistryScreen.this.onAccept.accept(this.name, this.item);
							SelectFromRegistryScreen.this.minecraft.setScreen(SelectFromRegistryScreen.this.parentScreen);
						} catch (Exception e) {
							SelectFromRegistryScreen.this.minecraft.setScreen(new MessageScreen<>("", e.getMessage(), SelectFromRegistryScreen.this.parentScreen, (button$2) -> SelectFromRegistryScreen.this.minecraft.setScreen(SelectFromRegistryScreen.this.parentScreen), 180, 70).autoCalculateHeight());
						}
						
						return true;
					}
					
					RegistryList.this.setSelected(this);
					
					return true;
				} else {
					return false;
				}
			}
		}
	}
}