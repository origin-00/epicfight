package yesman.epicfight.api.client.model;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.FileToIdConverter;
import java.util.Map;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.client.events.engine.RenderEngine;

public class ItemSkinsReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
	public static final ItemSkinsReloadListener INSTANCE = new ItemSkinsReloadListener();
	
	public ItemSkinsReloadListener() {
		super(ExtraCodecs.JSON, FileToIdConverter.json("item_skins"));
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profileFiller) {
		RenderEngine.getInstance().reloadItemRenderers(objectIn);
	}
}