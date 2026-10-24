package yesman.epicfight.client.renderer.patched.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelType;
import org.joml.Vector4f;
import yesman.epicfight.api.client.event.EpicFightClientEventHooks;
import yesman.epicfight.api.client.event.types.registry.RegisterPatchedRenderersEvent;
import yesman.epicfight.api.client.event.types.render.PrepareModelEvent;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.api.utils.math.Vec2i;
import yesman.epicfight.api.utils.math.Vec3f;
import yesman.epicfight.client.renderer.EpicFightRenderUtils;
import yesman.epicfight.client.renderer.LayerRenderer;
import yesman.epicfight.client.renderer.patched.layer.LayerUtil;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.client.renderer.patched.layer.RenderOriginalModelLayer;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.mixin.client.MixinLivingEntityRenderer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

public abstract class PatchedLivingEntityRenderer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<?>, R extends LivingEntityRenderer<E, ?, ?>, AM extends SkinnedMesh> extends PatchedEntityRenderer<E, T, R, AM> implements LayerRenderer<E, T, M> {
	protected final Map<Class<?>, PatchedLayer<E, T, M, ? extends RenderLayer<?, ?>>> patchedLayers = Maps.newHashMap();
	protected final List<PatchedLayer<E, T, M, ? extends RenderLayer<?, ?>>> customLayers = Lists.newArrayList();

	public PatchedLivingEntityRenderer(EntityRendererProvider.Context context, EntityType<?> entityType) {
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

	public PatchedLivingEntityRenderer<E, T, M, R, AM> initLayerLast(EntityRendererProvider.Context context, EntityType<?> entityType) {
		List<? extends RenderLayer<?, ?>> vanillaLayers = null;

		if (entityType == EntityType.PLAYER) {
			if (context.getEntityRenderDispatcher().playerRenderers.get(PlayerModelType.WIDE) instanceof LivingEntityRenderer<?, ?, ?> livingentityrenderer) {
				vanillaLayers = livingentityrenderer.layers;
			}
		} else {
			if (context.getEntityRenderDispatcher().renderers.get(entityType) instanceof LivingEntityRenderer<?, ?, ?> livingentityrenderer) {
				vanillaLayers = livingentityrenderer.layers;
			}
		}

		if (vanillaLayers != null) {
			for (RenderLayer<?, ?> layer : vanillaLayers) {
				Class<?> layerClass = layer.getClass();

				if (layerClass.isAnonymousClass()) {
					layerClass = layer.getClass().getSuperclass();
				}

				if (this.patchedLayers.containsKey(layerClass)) {
					continue;
				}

				this.addPatchedLayer(layerClass, new RenderOriginalModelLayer<> ("Root", new Vec3f(0.0F, this.getDefaultLayerHeightCorrection(), 0.0F), new Vec3f(0.0F, 0.0F, 0.0F)));
			}
		}

		return this;
	}

	@Override
	public void render(E entity, T entitypatch, R renderer, EntityRenderState entityRenderState, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		super.render(entity, entitypatch, renderer, entityRenderState, buffer, poseStack, packedLight, partialTicks);

		LivingEntityRenderState renderState = (LivingEntityRenderState)entityRenderState;
		MixinLivingEntityRenderer livingEntityRendererAccessor = (MixinLivingEntityRenderer)renderer;

		boolean isVisible = livingEntityRendererAccessor.invokeIsBodyVisible(renderState);
		boolean isVisibleToPlayer = !isVisible && !renderState.isInvisibleToPlayer;
		RenderType renderType = livingEntityRendererAccessor.invokeGetRenderType(renderState, isVisible, isVisibleToPlayer, renderState.appearsGlowing());
		Armature armature = entitypatch.getArmature();
		poseStack.pushPose();
		this.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);
		this.prepareVanillaModel(entity, renderer.getModel(), renderer, renderState, partialTicks);
		this.setArmaturePose(entitypatch, armature, partialTicks);

		if (renderType != null) {
			AM mesh = this.getMeshProvider(entitypatch).get();
			this.prepareModel(mesh, entity, entitypatch, renderer);
			PrepareModelEvent prepareModelEvent = new PrepareModelEvent(this, mesh, entitypatch, buffer, poseStack, packedLight, partialTicks);

			if (!EpicFightClientEventHooks.Render.PREPARE_MODEL_TO_RENDER.post(prepareModelEvent).isCanceled()) {
				Vector4f color = new Vector4f(1.0F, 1.0F, 1.0F, isVisibleToPlayer ? 0.15F : 1.0F);
				entitypatch.getEntityDecorations().modifyColor(color, partialTicks);

				int blockLight = (packedLight & 0xF0) >> 4;
				int skyLight = (packedLight & 0xF00000) >> 20;
				Vec2i lightUv = new Vec2i(blockLight, skyLight);
				entitypatch.getEntityDecorations().modifyLight(lightUv, partialTicks);
				int modifiedLight = LightCoordsUtil.pack(lightUv.x, lightUv.y);
				mesh.draw(poseStack, buffer, renderType, modifiedLight, color.x(), color.y(), color.z(), color.w(), this.getOverlayCoord(entity, entitypatch, renderState, partialTicks), armature, armature.getPoseMatrices());

				entitypatch.getEntityDecorations().listDecorationOverlays().forEach(decorationOverlay -> {
					if (decorationOverlay.shouldRender()) {
						Vector4f overlayColor = decorationOverlay.color(partialTicks);
						mesh.draw(poseStack, buffer, decorationOverlay.getRenderType(), modifiedLight, overlayColor.x(), overlayColor.y(), overlayColor.z(), overlayColor.w(), OverlayTexture.NO_OVERLAY, armature, armature.getPoseMatrices());
					}
				});
			}
		}

		if (!entity.isSpectator()) {
			this.renderLayer(renderer, entitypatch, entity, renderState, armature.getPoseMatrices(), buffer, poseStack, packedLight, partialTicks);
		}

		if (renderType != null) {
			if (EpicFightRenderUtils.shouldRenderHitBoxes()) {
				entitypatch.getClientAnimator().renderDebuggingInfoForAllLayers(poseStack, buffer, partialTicks);
			}
		}

		poseStack.popPose();
	}

	/// Poses the vanilla model for this frame. Since 1.21.2 models animate from the render state,
	/// which vanilla has already extracted for the entity; Epic Fight reads the posed parts for
	/// vanilla-model layers and animated vanilla mesh parts.
	@SuppressWarnings("unchecked")
	protected void prepareVanillaModel(E entity, EntityModel<?> model, R renderer, LivingEntityRenderState renderState, float partialTicks) {
		((EntityModel<LivingEntityRenderState>)model).setupAnim(renderState);
	}

	protected void prepareModel(AM mesh, E entity, T entitypatch, R renderer) {
		mesh.initialize();
	}

	protected void renderLayer(R renderer, T entitypatch, E entity, LivingEntityRenderState renderState, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
		float bob = renderState.ageInTicks;
		float yRot = renderState.yRot;
		float xRot = renderState.xRot;

		for (RenderLayer<?, ?> layer : renderer.layers) {
			Class<?> layerClass = layer.getClass();

			if (layerClass.isAnonymousClass()) {
				layerClass = layerClass.getSuperclass();
			}

			if (this.patchedLayers.containsKey(layerClass)) {
				this.patchedLayers.get(layerClass).renderLayer(entity, entitypatch, layer, renderState, poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
			}
		}

		for (PatchedLayer<E, T, M, ? extends RenderLayer<?, ?>> patchedLayer : this.customLayers) {
			patchedLayer.renderLayer(entity, entitypatch, null, renderState, poseStack, buffer, packedLight, poses, bob, yRot, xRot, partialTicks);
		}
	}

	protected int getOverlayCoord(E entity, T entitypatch, LivingEntityRenderState renderState, float partialTicks) {
		int initU = 0;
		int initV = OverlayTexture.v(renderState.hasRedOverlay);

		Vec2i coord = new Vec2i(initU, initV);
		entitypatch.getEntityDecorations().modifyOverlay(coord, partialTicks);

		return OverlayTexture.pack(coord.x, coord.y);
	}

	@Override
	public void mulPoseStack(PoseStack poseStack, Armature armature, E entity, T entitypatch, float partialTicks) {
		super.mulPoseStack(poseStack, armature, entity, entitypatch, partialTicks);

        if (entity.isCrouching()) {
			poseStack.translate(0.0D, 0.15D, 0.0D);
		}
	}

	@Override
	public void addPatchedLayer(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends RenderLayer<?, ?>> patchedLayer) {
		this.patchedLayers.putIfAbsent(originalLayerClass, patchedLayer);
	}

	/**
	 * Use this method in {@link RegisterPatchedRenderersEvent.ModifyEntity}}
	 */
	public void addPatchedLayerAlways(Class<?> originalLayerClass, PatchedLayer<E, T, M, ? extends RenderLayer<?, ?>> patchedLayer) {
		this.patchedLayers.put(originalLayerClass, patchedLayer);
	}

	@Override
	public void addCustomLayer(PatchedLayer<E, T, M, ? extends RenderLayer<?, ?>> patchedLayer) {
		this.customLayers.add(patchedLayer);
	}

	protected float getDefaultLayerHeightCorrection() {
		return 1.15F;
	}
}
