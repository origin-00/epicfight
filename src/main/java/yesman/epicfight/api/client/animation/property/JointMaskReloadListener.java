package yesman.epicfight.api.client.animation.property;

import net.minecraft.util.ExtraCodecs;
import net.minecraft.resources.FileToIdConverter;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import yesman.epicfight.api.client.animation.property.JointMask.BindModifier;
import yesman.epicfight.api.client.animation.property.JointMask.JointMaskSet;
import yesman.epicfight.main.EpicFightMod;

public class JointMaskReloadListener extends SimpleJsonResourceReloadListener<JsonElement> {
	private static final BiMap<Identifier, JointMaskSet> JOINT_MASKS = HashBiMap.create();
	private static final Map<String, JointMask.BindModifier> BIND_MODIFIERS = Maps.newHashMap();
    private static final Identifier NONE_MASK = EpicFightMod.identifier("none");
	
	static {
		BIND_MODIFIERS.put("keep_child_locrot", JointMask.KEEP_CHILD_LOCROT);
	}
	
	public static JointMaskSet getJointMaskEntry(String type) {
		Identifier rl = Identifier.parse(type);
		return JOINT_MASKS.getOrDefault(rl, JOINT_MASKS.get(NONE_MASK));
	}
	
	public static JointMaskSet getNoneMask() {
		return JOINT_MASKS.get(NONE_MASK);
	}
	
	public static Identifier getKey(JointMaskSet type) {
		return JOINT_MASKS.inverse().get(type);
	}
	
	public static Set<Map.Entry<Identifier, JointMaskSet>> entries() {
		return JOINT_MASKS.entrySet();
	}
	
	public JointMaskReloadListener() {
		super(ExtraCodecs.JSON, FileToIdConverter.json("animmodels/joint_mask"));
	}
	
	@Override
	protected void apply(Map<Identifier, JsonElement> objectIn, ResourceManager resourceManager, ProfilerFiller profileFiller) {
		JOINT_MASKS.clear();
		
		for (Map.Entry<Identifier, JsonElement> entry : objectIn.entrySet()) {
			Set<JointMask> masks = Sets.newHashSet();
			JsonObject object = entry.getValue().getAsJsonObject();
			JsonArray joints = object.getAsJsonArray("joints");
			JsonObject bindModifiers = object.has("bind_modifiers") ? object.getAsJsonObject("bind_modifiers") : null;
			
			for (JsonElement joint : joints) {
				String jointName = joint.getAsString();
				BindModifier modifier = null;
				
				if (bindModifiers != null) {
					String modifierName = bindModifiers.has(jointName) ? bindModifiers.get(jointName).getAsString() : null;
					modifier = BIND_MODIFIERS.get(modifierName);
				}
				
				masks.add(JointMask.of(jointName, modifier));
			}
			
			String path = entry.getKey().toString();
			Identifier key = Identifier.fromNamespaceAndPath(entry.getKey().getNamespace(), path.substring(path.lastIndexOf("/") + 1));
			
			JOINT_MASKS.put(key, JointMaskSet.of(masks));
		}
	}
}