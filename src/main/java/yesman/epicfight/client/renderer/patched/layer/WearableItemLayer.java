package yesman.epicfight.client.renderer.patched.layer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.asset.JsonAssetLoader;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.render.AnimatedArmorTextureEvent;
import yesman.epicfight.api.client.model.Mesh.DrawingFunction;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.transformer.HumanoidModelBaker;
import yesman.epicfight.api.exception.AssetLoadingException;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.ColorUtil;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.events.engine.RenderEngine;
import yesman.epicfight.client.mesh.HumanoidMesh;
import yesman.epicfight.client.renderer.submit.RecordingBufferSource;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/// Renders worn armor on the Epic Fight armature.
///
/// Since 1.21.2 armor is described by equipment assets: an item's [Equippable] names an asset
/// whose layers ([EquipmentClientInfo.Layer]) give the textures and tints, and each armor slot has
/// its own vanilla [HumanoidModel] with only that slot's parts visible. This layer follows
/// [net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer] for textures, tint, foil and
/// trims, drawing them on the armor mesh baked from (or replacing) the slot's model.
public class WearableItemLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>, AM extends HumanoidMesh> extends ModelRenderLayer<E, T, M, HumanoidArmorLayer<?, ?, ?>, AM> {
	private static final Map<Identifier, SkinnedMesh> ARMOR_MODELS = new HashMap<> ();

	public static void clearModels() {
		ARMOR_MODELS.values().stream().filter(v -> v != null).forEach(SkinnedMesh::destroy);
		ARMOR_MODELS.clear();
	}

	public static void putModel(Identifier rl, SkinnedMesh skinnedMesh) {
		ARMOR_MODELS.computeIfPresent(rl, (key, mesh) -> {
			if (mesh != skinnedMesh) mesh.destroy();
			return mesh;
		});

		ARMOR_MODELS.put(rl, skinnedMesh);
	}

	public static SkinnedMesh getCachedModel(Item item) {
		Identifier key = BuiltInRegistries.ITEM.getKey(item);
		return ARMOR_MODELS.get(key);
	}

	private final boolean firstPersonModel;
	private final TextureAtlas armorTrimAtlas;
	private final EquipmentAssetManager equipmentAssets;

	public WearableItemLayer(AssetAccessor<AM> meshProvider, boolean firstPersonModel, EntityRendererProvider.Context context) {
		super(meshProvider);

		this.firstPersonModel = firstPersonModel;
		this.armorTrimAtlas = context.getAtlas(AtlasIds.ARMOR_TRIMS);
		this.equipmentAssets = context.getEquipmentAssets();
	}

	private void renderArmor(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, SkinnedMesh model, Armature armature, float r, float g, float b, Identifier armorTexture, OpenMatrix4f[] poses) {
		model.draw(poseStack, multiBufferSource, RenderTypes.armorCutoutNoCull(armorTexture), packedLight, r, g, b, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}

	private void renderGlint(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, SkinnedMesh model, Armature armature, OpenMatrix4f[] poses) {
		model.draw(poseStack, multiBufferSource, RenderTypes.armorEntityGlint(), packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses);
	}

	private void renderTrim(PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, SkinnedMesh model, Armature armature, ArmorTrim armorTrim, EquipmentClientInfo.LayerType layerType, ResourceKey<EquipmentAsset> assetId, OpenMatrix4f[] poses) {
		TextureAtlasSprite sprite = this.armorTrimAtlas.getSprite(armorTrim.layerAssetId(layerType.trimAssetPrefix(), assetId));
		RenderType renderType = Sheets.armorTrimsSheet(armorTrim.pattern().value().decal());
		RecordingBufferSource.drawTriangles(multiBufferSource, renderType, buffer -> model.drawPosed(poseStack, sprite.wrap(buffer), DrawingFunction.NEW_ENTITY, packedLight, 1.0F, 1.0F, 1.0F, 1.0F, OverlayTexture.NO_OVERLAY, armature, poses));
	}

	@Override
	public void renderLayer(T entitypatch, E livingentity, HumanoidArmorLayer<?, ?, ?> vanillaLayer, LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource buffers, int packedLight, OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks) {
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
				continue;
			}

			boolean firstPersonChest = false;

			if (entitypatch.isFirstPerson() && this.firstPersonModel) {
				if (slot != EquipmentSlot.CHEST) {
					continue;
				} else {
					firstPersonChest = true;
				}
			}

			if (slot == EquipmentSlot.HEAD && this.firstPersonModel) {
				continue;
			}

			ItemStack itemstack = livingentity.getItemBySlot(slot);
			Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);

			// HumanoidArmorLayer#shouldRender
			if (equippable == null || equippable.slot() != slot || equippable.assetId().isEmpty()) {
				continue;
			}

			ResourceKey<EquipmentAsset> assetId = equippable.assetId().get();
			EquipmentClientInfo.LayerType layerType = renderState.isBaby ? EquipmentClientInfo.LayerType.HUMANOID_BABY : (innerModel(slot) ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID);
			HumanoidModel<?> vanillaModel = vanillaLayer == null ? null : (renderState.isBaby ? vanillaLayer.babyModelSet : vanillaLayer.modelSet).get(slot);
			Model<?> armorModel = vanillaModel == null ? null : IClientItemExtensions.of(itemstack).getGenericArmorModel(itemstack, layerType, vanillaModel);
			SkinnedMesh armorMesh = this.getArmorModel(vanillaModel, armorModel, livingentity, itemstack, slot);

			if (armorMesh == null) {
				continue;
			}

			poseStack.pushPose();

			if (armorModel instanceof HumanoidModel<?> humanoidModel) {
				// Baked armor follows the armature, so the vanilla parts stay in their rest pose.
				humanoidModel.head.loadPose(humanoidModel.head.getInitialPose());
				humanoidModel.hat.loadPose(humanoidModel.hat.getInitialPose());
				humanoidModel.body.loadPose(humanoidModel.body.getInitialPose());
				humanoidModel.leftArm.loadPose(humanoidModel.leftArm.getInitialPose());
				humanoidModel.rightArm.loadPose(humanoidModel.rightArm.getInitialPose());
				humanoidModel.leftLeg.loadPose(humanoidModel.leftLeg.getInitialPose());
				humanoidModel.rightLeg.loadPose(humanoidModel.rightLeg.getInitialPose());
			}

			armorMesh.initialize();

			if (firstPersonChest) {
				armorMesh.getAllParts().forEach(part -> part.setHidden(true));

				if (armorMesh.hasPart("leftArm")) {
					armorMesh.getPart("leftArm").setHidden(false);
				}

				if (armorMesh.hasPart("rightArm")) {
					armorMesh.getPart("rightArm").setHidden(false);
				}
			}

			AnimatedArmorTextureEvent textureEvent = EpicFightClientEventHooks.Render.ANIMATED_ARMOR_TEXTURE.post(new AnimatedArmorTextureEvent(livingentity, itemstack, slot, vanillaModel));
			Identifier overriddenTexture = textureEvent.getResultLocation();

			/**
			 * Copy from {@link net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer#renderLayers}
			 */
			IClientItemExtensions extensions = IClientItemExtensions.of(itemstack);
			List<EquipmentClientInfo.Layer> layers = this.equipmentAssets.get(assetId).getLayers(layerType);
			int dyeColor = extensions.getDefaultDyeColor(itemstack);
			boolean renderFoil = itemstack.hasFoil();
			int layerIdx = 0;

			for (EquipmentClientInfo.Layer layer : layers) {
				int packedColor = extensions.getArmorLayerTintColor(itemstack, layer, layerIdx, dyeColor);

				if (packedColor != 0) {
					Vector4f color = ColorUtil.unpackToARGBF(packedColor);
					Identifier texture = overriddenTexture != null
							? overriddenTexture
							: ParseUtil.tryGetOr(() -> armorMesh.getRenderProperties().customTexturePath(), () -> ClientHooks.getArmorTexture(itemstack, layerType, layer, layer.getTextureLocation(layerType)));
					this.renderArmor(poseStack, buffers, packedLight, armorMesh, entitypatch.getArmature(), color.x, color.y, color.z, texture, poses);

					if (renderFoil) {
						this.renderGlint(poseStack, buffers, packedLight, armorMesh, entitypatch.getArmature(), poses);
					}

					renderFoil = false;
				}

				layerIdx++;
			}

			ArmorTrim armorTrim = itemstack.get(DataComponents.TRIM);

			if (armorTrim != null && layerType != EquipmentClientInfo.LayerType.HUMANOID_BABY) {
				this.renderTrim(poseStack, buffers, packedLight, armorMesh, entitypatch.getArmature(), armorTrim, layerType, assetId, poses);
			}

			poseStack.popPose();
		}
	}

	private SkinnedMesh getArmorModel(@Nullable HumanoidModel<?> originalModel, @Nullable Model<?> neoforgeArmorModel, E entityliving, ItemStack itemstack, EquipmentSlot slot) {
		Item item = itemstack.getItem();
		Identifier registryName = BuiltInRegistries.ITEM.getKey(item);

		if (ARMOR_MODELS.containsKey(registryName) && !RenderEngine.getInstance().shouldRenderVanillaModel()) {
			return ARMOR_MODELS.get(registryName);
		} else {
			ResourceManager resourceManager = Minecraft.getInstance().getResourceManager();
			Identifier rl = Identifier.fromNamespaceAndPath(registryName.getNamespace(), "animmodels/armor/" + registryName.getPath() + ".json");
			SkinnedMesh skinnedMesh = null;

			if (resourceManager.getResource(rl).isPresent()) {
				try {
					JsonAssetLoader modelLoader = new JsonAssetLoader(resourceManager, rl);
					skinnedMesh = modelLoader.loadSkinnedMesh(SkinnedMesh::new);
				} catch (AssetLoadingException e) {
					e.printStackTrace();
					skinnedMesh = null;
				}
			} else {
				// Since 1.21.2 each slot has its own vanilla armor model with only that slot's parts
				// visible, so it can be baked as is.
				skinnedMesh = HumanoidModelBaker.bakeArmor(entityliving, itemstack, slot, originalModel, neoforgeArmorModel, this.mesh.get());
			}

			putModel(registryName, skinnedMesh);

			return skinnedMesh;
		}
	}

	/**
	 * Copy from {@link HumanoidArmorLayer#usesInnerModel}
	 */
	private static boolean innerModel(EquipmentSlot slot) {
		return slot == EquipmentSlot.LEGS;
	}

	/// The texture of the first layer of an armor piece's equipment asset, or `null` when the item
	/// is not wearable armor for `slot`. Replaces the pre-1.21.2 `textures/models/armor/<material>_layer_<n>.png` lookup.
	public static @Nullable Identifier getArmorResource(LivingEntity entity, ItemStack itemstack, EquipmentSlot slot) {
		Equippable equippable = itemstack.get(DataComponents.EQUIPPABLE);

		if (equippable == null || equippable.assetId().isEmpty()) {
			return null;
		}

		EquipmentClientInfo.LayerType layerType = entity.isBaby() ? EquipmentClientInfo.LayerType.HUMANOID_BABY : (innerModel(slot) ? EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS : EquipmentClientInfo.LayerType.HUMANOID);
		List<EquipmentClientInfo.Layer> layers = Minecraft.getInstance().getEntityRenderDispatcher().equipmentAssets.get(equippable.assetId().get()).getLayers(layerType);

		return layers.isEmpty() ? null : layers.getFirst().getTextureLocation(layerType);
	}
}
