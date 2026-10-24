package yesman.epicfight.client.renderer.patched.entity;

import yesman.epicfight.client.renderer.EpicFightRenderUtils;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.render.PrepareModelEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.LayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

/**
 * This class is for LivingEntity with renderer that doesn't extend LivingEntityRenderer (e.g. Geckolib renderer based entities)
 */
public class PresetRenderer extends PatchedEntityRenderer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityRenderer<LivingEntity, ?>, SkinnedMesh> implements LayerRenderer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<?>> {
	private final LivingEntityRenderer<LivingEntity, ?, ?> presetRenderer;
	protected final Map<Class<?>, PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<?>, ? extends RenderLayer<?, ?>>> patchedLayers = Maps.newHashMap();
	protected final List<PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<?>, ? extends RenderLayer<?, ?>>> customLayers = Lists.newArrayList();
	protected final AssetAccessor<SkinnedMesh> mesh;
	
	public PresetRenderer(EntityRendererProvider.Context context, EntityType<?> entityType, LivingEntityRenderer<LivingEntity, ?, ?> renderer, AssetAccessor<SkinnedMesh> mesh) {
		this.presetRenderer = renderer;
		this.mesh = mesh;
		
		Identifier type = EntityType.getKey(entityType);
		FileToIdConverter filetoidconverter = FileToIdConverter.json("animated_layers/" + type.getPath());
		List<Pair<Identifier, JsonElement>> layers = Lists.newArrayList();
		
		for (Map.Entry<Identifier, Resource> entry : filetoidconverter.listMatchingResources(context.getResourceManager()).entrySet()) {
			Reader reader = null;
			
			try {
				reader = entry.getValue().openAsReader();
				JsonElement jsonelement = GsonHelper.fromJson(new GsonBuilder().create(), reader, JsonElement.class);
				layers.add(Pair.of(entry.getKey(), jsonelement));
			} catch (IllegalArgumentException | IOException | JsonParseException jsonparseexception) {
				EpicFightMod.LOGGER.error("Failed to parse layer file {} for {}", entry.getKey(), type);
				jsonparseexception.printStackTrace();
			} finally {
				try {
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
				}
			}
		}
		
		LayerUtil.addLayer(this, entityType, layers);
	}
	
	@Override
	public void render(LivingEntity entity, LivingEntityPatch<LivingEntity> entitypatch, EntityRenderer<LivingEntity, ?> renderer, EntityRenderState entityRenderState, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		super.render(entity, entitypatch, renderer, entityRenderState, buffer, poseStack, packedLight, partialTicks);

		// The entity's own renderer is not a living entity renderer: render types, the vanilla model
		// and layers come from the preset renderer, so it needs its own state for this frame.
		LivingEntityRenderState renderState = (LivingEntityRenderState)((EntityRenderer<LivingEntity, ?>)this.presetRenderer).createRenderState(entity, partialTicks);
		MixinLivingEntityRenderer livingEntityRendererAccessor = (MixinLivingEntityRenderer)this.presetRenderer;

		boolean isVisible = livingEntityRendererAccessor.invokeIsBodyVisible(renderState);
		boolean isVisibleToPlayer = !isVisible && !renderState.isInvisibleToPlayer;
		RenderType renderType = livingEntityRendererAccessor.invokeGetRenderType(renderState, isVisible, isVisibleToPlayer, renderState.appearsGlowing());
		Armature armature = entitypatch.getArmature();
		poseStack.pushPose();
		this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
		this.setArmaturePose(entitypatch, armature, partialTicks);

		if (renderType != null) {
		    this.prepareVanillaModel(entity, this.presetRenderer.getModel(), this.presetRenderer, renderState, partialTicks);
			SkinnedMesh mesh = this.getMeshProvider(entitypatch).get();
			this.prepareModel(mesh, entity, entitypatch, this.presetRenderer);
			PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);

            if (!EpicFightClientEventHooks.Render.PREPARE_MODEL_TO_RENDER.post(prepareModelEvent).isCanceled()) {
                mesh.draw(poseStack, buffer, renderType, packedLight, 1.0F, 1.0F, 1.0F, isVisibleToPlayer ? 0.15F : 1.0F, this.getOverlayCoord(entity, entitypatch, partialTicks), armature, armature.getPoseMatrices());
            }
		}

		if (!entity.isSpectator()) {
			this.renderLayer(this.presetRenderer, entitypatch, entity, armature.getPoseMatrices(), buffer, renderState, poseStack, packedLight, partialTicks);
		}

		if (renderType != null) {
			if (EpicFightRenderUtils.shouldRenderHitBoxes()) {
				entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
			}
		}

		poseStack.popPose();
	}

	/// Poses the preset's vanilla model from the render state (models animate from states since 1.21.2).
	@SuppressWarnings("unchecked")
	protected void prepareVanillaModel(LivingEntity entityIn, EntityModel<?> model, LivingEntityRenderer<LivingEntity, ?, ?> renderer, LivingEntityRenderState renderState, float partialTicks) {
		((EntityModel<LivingEntityRenderState>)model).setupAnim(renderState);
	}

	protected void prepareModel(SkinnedMesh mesh, LivingEntity entity, LivingEntityPatch<LivingEntity> entitypatch, LivingEntityRenderer<LivingEntity, ?, ?> renderer) {
		mesh.initialize();
	}
	
	protected void renderLayer(LivingEntityRenderer<LivingEntity, ?, ?> renderer, LivingEntityPatch<LivingEntity> entitypatch, LivingEntity entity, OpenMatrix4f[] poses, MultiBufferSource buffer, LivingEntityRenderState renderState, PoseStack poseStack, int packedLight, float partialTicks) {
		float f2 = renderState.yRot;
		float f7 = renderState.xRot;
		float bob = renderState.ageInTicks;
		
		for (RenderLayer<?, ?> layer : renderer.layers) {
			Class<?> layerClass = layer.getClass();
			
			if (layerClass.isAnonymousClass()) {
				layerClass = layerClass.getSuperclass();
			}
			
			if (this.patchedLayers.containsKey(layerClass)) {
				this.patchedLayers.get(layerClass).renderLayer(entity, entitypatch, layer, renderState, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
			}
		}
		
		for (PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<?>, ? extends RenderLayer<?, ?>> patchedLayer : this.customLayers) {
			patchedLayer.renderLayer(entity, entitypatch, null, renderState, poseStack, buffer, packedLight, poses, bob, f2, f7, partialTicks);
		}
	}
	
	protected int getOverlayCoord(LivingEntity entity, LivingEntityPatch<LivingEntity> entitypatch, float partialTicks) {
		return OverlayTexture.pack(0, OverlayTexture.v(entity.hurtTime > 5));
	}
	
	@Override
	public void addPatchedLayer(Class<?> originalLayerClass, PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<?>, ? extends RenderLayer<?, ?>> patchedLayer) {
		this.patchedLayers.putIfAbsent(originalLayerClass, patchedLayer);
	}
	
	@Override
	public void addCustomLayer(PatchedLayer<LivingEntity, LivingEntityPatch<LivingEntity>, EntityModel<?>, ? extends RenderLayer<?, ?>> patchedLayer) {
		this.customLayers.add(patchedLayer);
	}
	
	@Override
	public AssetAccessor<SkinnedMesh> getDefaultMesh() {
		return this.mesh;
	}
}
