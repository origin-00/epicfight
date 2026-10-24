package yesman.epicfight.client.gui.widgets;

import yesman.epicfight.EpicFight;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.ARGB;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import yesman.epicfight.client.renderer.submit.RecordingBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.gui.components.AbstractScrollArea;
import yesman.epicfight.client.gui.render.ModelPreviewRenderer;
import yesman.epicfight.client.gui.render.ModelPreviewRenderState;
import net.minecraft.client.input.MouseButtonEvent;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import yesman.epicfight.api.animation.*;
import yesman.epicfight.api.animation.types.*;
import yesman.epicfight.api.animation.types.AttackAnimation.Phase;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.api.client.animation.Layer;
import yesman.epicfight.api.client.animation.property.ClientAnimationProperties;
import yesman.epicfight.api.client.animation.property.TrailInfo;
import yesman.epicfight.api.client.model.Mesh;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.SoftBodyTranslatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulatable;
import yesman.epicfight.api.client.physics.cloth.ClothSimulator;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.physics.PhysicsSimulator;
import yesman.epicfight.api.physics.SimulatableObject;
import yesman.epicfight.api.physics.SimulationTypes;
import yesman.epicfight.api.physics.bezier.CubicBezierCurve;
import yesman.epicfight.api.utils.math.*;
import yesman.epicfight.client.gui.widgets.common.AnchoredWidget;
import yesman.epicfight.client.particle.AnimationTrailParticle;
import yesman.epicfight.client.renderer.EpicFightShaders;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.generated.LangKeys;
import yesman.epicfight.main.EpicFightSharedConstants;
import yesman.epicfight.world.capabilities.entitypatch.Faction;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.damagesource.StunType;

import java.util.*;
import java.util.function.Function;

public class ModelPreviewer extends AbstractContainerWidget implements AnchoredWidget {
	private static final Identifier UNTEXTURED_FIGURE = EpicFight.identifier("textures/common/white.png");
	private static final int FULL_BRIGHT = 0xF000F0;

	/// AbstractContainerWidget is a scroll area since 1.21.9; the previewer never scrolls.
	@Override
	protected int contentHeight() {
		return this.getHeight();
	}

	private int backgroundColor() {
		// The render target's default clear color was (0.1552, 0.1552, 0.1552).
		return this.backgroundClearColor == null ? 0xFF282828 : ARGB.colorFromFloat(this.backgroundClearColor.w, this.backgroundClearColor.x, this.backgroundClearColor.y, this.backgroundClearColor.z);
	}

	private final List<AssetAccessor<? extends StaticAnimation>> animationsToPlay = new ArrayList<> ();
	private final List<CustomTrailParticle> trailParticles = new ArrayList<> ();
    private final List<TrailInfo> trailInfoList = new ArrayList<> ();

	private final CheckBox showColliderCheckbox;
	private final CheckBox showItemCheckbox;
	private final CheckBox showTrailCheckbox;

	private double zoom = -3.0D;
    private float xRot = 0.0F;
    private float yRotO = 180.0F;
    private float yRot = 180.0F;
    private float xMove = 0.0F;
    private float yMove = 0.0F;
	private int index;
	private float attackTimeBegin;
	private float attackTimeEnd;

    @Nullable
	private FakeEntityPatch entitypatch;
    @Nullable
	private NoEntityAnimator animator;
    @Nullable
	private AssetAccessor<? extends SkinnedMesh> mesh;
    @Nullable
	private AssetAccessor<? extends Armature> armature;
    @Nullable
	private Identifier figureTexture;

    @Nullable
	private Joint colliderJoint;
    @Nullable
	private Collider collider;
    @Nullable
	private Item item;

    @Nullable
	private Vec4f backgroundClearColor;
	private boolean cameraMoveEnabled = true;
	private boolean cameraRotationEnabled = true;
	private boolean zoomingCameraEnabled = true;

    @Nullable
    private ClothSimulator clothSimulator;
    @Nullable
    private SoftBodyTranslatable cloakMesh;
    @Nullable
    private Identifier cloakTexture;
	private Vec3f cloakColor = new Vec3f(1.0F, 1.0F, 1.0F);

	public ModelPreviewer(
        Font font,
        int x1,
        int x2,
        int y1,
        int y2,
        AnchoredWidget.HorizontalAnchorType horizontalAnchor,
        AnchoredWidget.VerticalAnchorType verticalAnchor,
        @Nullable AssetAccessor<? extends Armature> armature,
        @Nullable AssetAccessor<? extends SkinnedMesh> mesh
    ) {
		super(x1, y1, x2, y2, Component.literal(""), AbstractScrollArea.defaultSettings(0));
		
		if (armature != null) {
			this.entitypatch = new FakeEntityPatch(armature.get());
			this.animator = new NoEntityAnimator(this.entitypatch);
			this.entitypatch.initAnimator(this.animator);
		}
		
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.horizontalAnchorType = horizontalAnchor;
		this.verticalAnchorType = verticalAnchor;
		this.mesh = mesh;
		this.armature = armature;

        this.showColliderCheckbox = new CheckBox(
            font,
            0,
            60,
            0,
            10,
            AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH,
            AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
            () -> false,
            val -> {},
            Component.translatable(LangKeys.GUI_WIDGET_MODEL_PREVIEWER_COLLIDER),
            true
        );

        this.showItemCheckbox = new CheckBox(
            font,
            0,
            40,
            0,
            10,
            AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH,
            AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
            () -> false,
            val -> {},
            Component.translatable(LangKeys.GUI_WIDGET_MODEL_PREVIEWER_ITEM),
            true
        );

        this.showTrailCheckbox = new CheckBox(
            font,
            0,
            40,
            0,
            10,
            AnchoredWidget.HorizontalAnchorType.RIGHT_WIDTH,
            AnchoredWidget.VerticalAnchorType.TOP_HEIGHT,
            () -> false,
            val -> {},
            Component.translatable(LangKeys.GUI_WIDGET_MODEL_PREVIEWER_TRAIL),
            true
        );


	}

	public void setMesh(AssetAccessor<? extends SkinnedMesh> mesh) {
		this.mesh = mesh;
	}

	public void setFigureTexture(Identifier texture) {
		this.figureTexture = texture;
	}

	public void initCloakInfo(SoftBodyTranslatable cloakMesh, Identifier cloakTexture, @Nullable ClothSimulator.ClothObjectBuilder builder) {
		this.cloakMesh = cloakMesh;
		this.cloakTexture = cloakTexture;
		
		if (builder != null) {
			this.clothSimulator = new ClothSimulator();
			this.clothSimulator.runWhen(ClothSimulator.MODELPREVIEWER_CLOAK, cloakMesh, builder, () -> true);
		}
	}

	public void removeCloak() {
		if (this.clothSimulator != null) {
			this.clothSimulator.stop(ClothSimulator.MODELPREVIEWER_CLOAK);
			this.clothSimulator = null;
		}
		
		this.cloakMesh = null;
		this.cloakTexture = null;
	}
	
	public void setCloakColor(int colorCode) {
		float b = (colorCode & 255) / 255.0F;
		float g = ((colorCode & 65280) >> 8) / 255.0F;
		float r = ((colorCode & 16711680) >> 16) / 255.0F;
		
		this.cloakColor = new Vec3f(r, g, b);
	}
	
	public void setCloakColor(float r, float g, float b) {
		this.cloakColor = new Vec3f(r, g, b);
	}
	
	public Vec3f getCloakColor() {
		return this.cloakColor;
	}

    @Nullable
	public SoftBodyTranslatable getCloakMesh() {
		return this.cloakMesh;
	}
	
	public void setCloakTexture(Identifier cloakTexture) {
		this.cloakTexture = cloakTexture;
	}

    @Nullable
	public Identifier getCloakTexture() {
		return this.cloakTexture;
	}

    @Nullable
	public AssetAccessor<? extends Armature> getArmature() {
		return this.armature;
	}

    @Nullable
	public AssetAccessor<? extends SkinnedMesh> getMesh() {
		return this.mesh;
	}

    @Nullable
	public Animator getAnimator() {
		return this.animator;
	}
	
	public void setCameraTransform(double zoom, float xRot, float yRot, float xMove, float yMove) {
		this.zoom = zoom;
		this.xRot = xRot;
		this.yRotO = yRot;
		this.yRot = yRot;
		this.xMove = xMove;
		this.yMove = yMove;
	}
	
	public void setCollider(Collider collider) {
		this.collider = collider;
	}
	
	public void setCollider(Collider collider, Joint joint) {
		this.collider = collider;
		this.colliderJoint = joint;
	}
	
	public void setColliderJoint(Joint joint) {
		this.colliderJoint = joint;
	}
	
	public void setTrailInfo(TrailInfo... trailInfos) {
		this.trailInfoList.clear();
        Collections.addAll(this.trailInfoList, trailInfos);
	}
	
	public void setItemToRender(Item item) {
		this.item = item;
	}
	
	public void setAttackTimeBegin(float attackTimeBegin) {
		this.attackTimeBegin = attackTimeBegin;
	}
	
	public void setAttackTimeEnd(float attackTimeEnd) {
		this.attackTimeEnd = attackTimeEnd;
	}
	
	public void setBackgroundClearColor(Vec4f clearColor) {
		this.backgroundClearColor = clearColor;
	}
	
	public void enableCameraControll(boolean enable) {
		this.cameraMoveEnabled = enable;
		this.cameraRotationEnabled = enable;
		this.zoomingCameraEnabled = enable;
	}
	
	public void enableCameraMove(boolean enable) {
		this.cameraMoveEnabled = enable;
	}
	
	public void enableCameraRotation(boolean enable) {
		this.cameraRotationEnabled = enable;
	}
	
	public void enableZoomingCamera(boolean enable) {
		this.zoomingCameraEnabled = enable;
	}
	
	public void addAnimationToPlay(AssetAccessor<? extends StaticAnimation> animation) {
        // Blocks crashes for exceptional cases
        if (animation == null) {
            return;
        }

		if (this.index == -1) {
			this.index = 0;
		}
		
		this.animationsToPlay.add(animation);
        if (this.animator != null) this.animator.playAnimation(animation, 0.0F);
	}

	public void removeAnimationPlayingAnimation(AssetAccessor<? extends StaticAnimation> animation) {
		this.animationsToPlay.remove(animation);
	}
	
	public void clearAnimations() {
		this.index = -1;
		this.animationsToPlay.clear();

        if (this.animator != null) {
            this.animator.playAnimation(Animations.EMPTY_ANIMATION, 0.0F);

            this.animator.iterAllLayers((layer) -> {
                layer.off(this.animator.getEntityPatch());
            });

            this.animator.playAnimation(Animations.OFF_ANIMATION_HIGHEST, 0.0F);
            this.animator.playAnimation(Animations.OFF_ANIMATION_MIDDLE, 0.0F);
            this.animator.playAnimation(Animations.OFF_ANIMATION_LOWEST, 0.0F);
        }
	}

	public void restartAnimations() {
		this.index = 0;
		
		if (this.animationsToPlay.isEmpty()) {
			this.index = -1;
			return;
		}
		
		if (this.animator != null) this.animator.playAnimation(this.animationsToPlay.getFirst(), 0.0F);
	}
	
	public void tick() {
		if (this.animator != null) {
			this.animator.tick();
		}
		
		this.yRotO = this.yRot;
		this.trailParticles.forEach(CustomTrailParticle::tick);
		this.trailParticles.removeIf(trail -> !trail.isAlive());
		
		if (this.entitypatch != null && this.clothSimulator != null) {
			this.clothSimulator.tick(this.entitypatch);
		}
	}

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(this.showColliderCheckbox, this.showItemCheckbox, this.showTrailCheckbox);
    }

    @Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double x = event.x();
		double y = event.y();
		int button = event.button();
		if (this.active && this.visible) {
			if (this.isValidClickButton(event.buttonInfo())) {
				boolean flag = this.isMouseOver(x, y);
				
				if (flag) {
					this.playDownSound(Minecraft.getInstance().getSoundManager());
					
					if (this.item != null) {
						if (this.showItemCheckbox.mouseClicked(event, doubleClick)) {
							return true;
						}
					}
					
					if (!this.trailInfoList.isEmpty()) {
						if (this.showTrailCheckbox.mouseClicked(event, doubleClick)) {
							return true;
						}
					}
					
					if (this.collider != null) {
						if (this.showColliderCheckbox.mouseClicked(event, doubleClick)) {
							return true;
						}
					}
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		double mouseX = event.x();
		double mouseY = event.y();
		int button = event.button();
		if (this.isMouseOver(mouseX, mouseY)) {
			if (button == 0) {
				if (this.cameraRotationEnabled) {
					this.xRot = (float)Mth.clamp(this.xRot + dy * 2.5D, -30.0D, 45.0D);
					this.yRot += (float)(dx * 2.5F);
				}
			} else if (button == 2) {
				if (this.cameraMoveEnabled) {
					this.xMove += (float)((float)dx * 0.015F * -this.zoom);
					this.yMove += (float)(-(float)dy * 0.015F * -this.zoom);
				}
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double x, double y, double xScroll, double yScroll) {
		if (this.zoomingCameraEnabled) {
			this.zoom = Mth.clamp(this.zoom + yScroll * 0.5D, -20.0D, -0.5D);
			return true;
		}
		
		return false;
	}
	
	/// Draws the figure in view space; called by [ModelPreviewRenderer] while the GUI renders.
	///
	/// Up to 1.21.5 this drew straight to a private render target with immediate GL state. Meshes are
	/// drawn with entity render types now (lit, full brightness) and item models through the feature
	/// dispatcher.
	protected void renderFigure(PoseStack poseStack, MultiBufferSource.BufferSource buffers, SubmitNodeCollector collector) {
        // Trick! we want to use advancing partial tick even tho the game is paused (in levels)
        final float partialTicks = ((DeltaTracker.Timer)Minecraft.getInstance().getDeltaTracker()).deltaTickResidual;

		if (this.entitypatch != null && this.mesh != null && this.mesh.get() != null) {
			RenderType figureRenderType = this.figureTexture != null ? RenderTypes.entityCutout(this.figureTexture) : RenderTypes.entitySolid(UNTEXTURED_FIGURE);

			if (this.animator != null) {
				Pose pose = this.animator.getPose(partialTicks);
				this.mesh.get().initialize();
				OpenMatrix4f[] poseMatrices = this.entitypatch.getArmature().getPoseAsTransformMatrix(pose, false);
				this.mesh.get().draw(poseStack, buffers, figureRenderType, Mesh.DrawingFunction.NEW_ENTITY, FULL_BRIGHT, 0.9411F, 0.9411F, 0.9411F, 1.0F, OverlayTexture.NO_OVERLAY, this.entitypatch.getArmature(), poseMatrices);

				if (this.item != null && this.showItemCheckbox.getWidgetValue()) {
					ItemStack itemstack = new ItemStack(this.item);
					OpenMatrix4f correction = new OpenMatrix4f().translate(0F, 0F, -0.13F).rotateDeg(-90.0F, Vec3f.X_AXIS);
					OpenMatrix4f handTransform = correction.mulFront(this.entitypatch.getArmature().getBoundTransformFor(pose, this.getArmature().get().searchJointByName("Tool_R")));

					poseStack.pushPose();
					MathUtils.mulStack(poseStack, handTransform);
					ItemStackRenderState itemRenderState = new ItemStackRenderState();
					Minecraft.getInstance().getItemModelResolver().updateForTopItem(itemRenderState, itemstack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, null, null, 0);
					itemRenderState.submit(poseStack, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
					poseStack.popPose();
				}

				if (!this.trailParticles.isEmpty() && this.showTrailCheckbox.getWidgetValue()) {
					for (CustomTrailParticle trail : this.trailParticles) {
						// Trails apply the preview camera themselves (CustomTrailParticle#setupPoseStack)
						trail.render(buffers.getBuffer(trail.getRenderType()), null, partialTicks);
					}
				}

				if (this.collider != null && this.showColliderCheckbox.getWidgetValue()) {
					VertexConsumer colliderBuffer = buffers.getBuffer(this.collider.getRenderType());
					AnimationPlayer player = this.animator.getPlayerFor(null);
					float elapsedTime = player.getPrevElapsedTime() + (player.getElapsedTime() - player.getPrevElapsedTime()) * partialTicks;
					boolean red = elapsedTime >= this.attackTimeBegin && elapsedTime <= this.attackTimeEnd;

					if (this.colliderJoint != null) {
						Pose prevPose = this.animator.getPose(0.0F);
						Pose currentPose = this.animator.getPose(1.0F);
						this.collider.drawInternal(poseStack, colliderBuffer, this.entitypatch.getArmature(), this.colliderJoint, prevPose, currentPose, partialTicks, red ? 0xFFFF0000 : -1);
					} else {
						DynamicAnimation animation = player.getAnimation().get();

						if (animation instanceof AttackAnimation attackanimation) {
							Phase phase = attackanimation.getPhaseByTime(elapsedTime);

							for (AttackAnimation.JointColliderPair pair : phase.getColliders()) {
								Pose prevPose = animation.getRawPose(player.getPrevElapsedTime());
								Pose currentPose = animation.getRawPose(player.getElapsedTime());
								this.collider.drawInternal(poseStack, colliderBuffer, this.entitypatch.getArmature(), pair.getFirst(), prevPose, currentPose, partialTicks, -1);
							}
						}
					}
				}
			} else {
				this.mesh.get().initialize();
				RecordingBufferSource.drawTriangles(buffers, figureRenderType, buffer -> this.mesh.get().draw(poseStack, buffer, Mesh.DrawingFunction.NEW_ENTITY, FULL_BRIGHT, 0.9411F, 0.9411F, 0.9411F, 1.0F, OverlayTexture.NO_OVERLAY));
			}
		}

		if (this.cloakMesh != null && this.cloakTexture != null) {
			RenderType cloakRenderType = RenderTypes.entityCutout(this.cloakTexture);
			this.cloakMesh.getOriginalMesh().initialize();

			if (this.animator != null && this.entitypatch != null && this.clothSimulator != null) {
				this.clothSimulator.getRunningObject(ClothSimulator.MODELPREVIEWER_CLOAK).ifPresent((clothObj) -> {
		            Function<Float, OpenMatrix4f> partialColliderTransformProvider = (partialFrame) -> {
						Vec3 pos = this.entitypatch.getAccuratePartialLocation(partialFrame);
						float yRotLerp = this.entitypatch.getAccurateYRot(partialFrame);
						return OpenMatrix4f.createTranslation((float)pos.x, (float)pos.y, (float)pos.z).rotateDeg(180.0F - yRotLerp, Vec3f.Y_AXIS);
		            };

		            Pose pose = this.animator.getPose(partialTicks);
		            this.entitypatch.getArmature().setPose(pose);

					clothObj.tick(this.entitypatch, partialColliderTransformProvider, partialTicks, this.entitypatch.getArmature(), this.entitypatch.getArmature().getPoseMatrices());

					poseStack.pushPose();
					poseStack.mulPose(Axis.YP.rotationDegrees(this.entitypatch.getYRot() - 180.0F));
					RecordingBufferSource.drawTriangles(buffers, cloakRenderType, buffer -> clothObj.drawPosed(poseStack, buffer, Mesh.DrawingFunction.NEW_ENTITY, FULL_BRIGHT, this.cloakColor.x, this.cloakColor.y, this.cloakColor.z, 1.0F, OverlayTexture.NO_OVERLAY, this.entitypatch.getArmature(), this.entitypatch.getArmature().getPoseMatrices()));
					poseStack.popPose();
				});
			} else {
				RecordingBufferSource.drawTriangles(buffers, cloakRenderType, buffer -> ((Mesh)this.cloakMesh).draw(poseStack, buffer, Mesh.DrawingFunction.NEW_ENTITY, FULL_BRIGHT, this.cloakColor.x, this.cloakColor.y, this.cloakColor.z, 1.0F, OverlayTexture.NO_OVERLAY));
			}
		}
	}
	
	@Override
	public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		// The scene is drawn when the GUI renders its picture-in-picture elements (1.21.6+), over the background.
		int x0 = this.getX(), y0 = this.getY(), x1 = this.getX() + this.getWidth(), y1 = this.getY() + this.getHeight();
		guiGraphics.fill(x0, y0, x1, y1, this.backgroundColor());
		guiGraphics.submitPictureInPictureRenderState(new ModelPreviewRenderState((poseStack, bufferSource, collector) -> {
			poseStack.translate(this.xMove, this.yMove - 1.0D, this.zoom);
			poseStack.mulPose(Axis.XP.rotationDegrees(this.xRot));
			poseStack.mulPose(Axis.YP.rotationDegrees(this.yRot));
			this.renderFigure(poseStack, bufferSource, collector);
		}, x0, y0, x1, y1, guiGraphics.peekScissorStack()));
		
		if (this.animator != null) {
			// Controls visibilities of each widget
			int top = this.getY() + 6;
			int right = this.getX() + this.getWidth() - 2;
			
			if (!this.trailInfoList.isEmpty()) {
				right -= this.showTrailCheckbox.getWidth();
				
				this.showTrailCheckbox.setX(right);
				this.showTrailCheckbox.setY(top);
				this.showTrailCheckbox.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
			}
			
			if (this.item != null) {
				right -= this.showItemCheckbox.getWidth();
				
				this.showItemCheckbox.setX(right);
				this.showItemCheckbox.setY(top);
				this.showItemCheckbox.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
			}
			
			if (this.collider != null) {
				right -= this.showColliderCheckbox.getWidth();
				
				this.showColliderCheckbox.setX(right);
				this.showColliderCheckbox.setY(top);
				this.showColliderCheckbox.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTicks);
			}
		}
		
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput narrationElementInput) {
		narrationElementInput.add(NarratedElementType.TITLE, this.createNarrationMessage());
	}

	public void onDestroy() {
	}

	public class FakeEntityPatch extends LivingEntityPatch<LivingEntity> implements SimulatableObject, ClothSimulatable {
		public FakeEntityPatch(Armature armature) {
			super(null);
			this.armature = armature.deepCopy();
		}
		
		public void setAnimator() {
			this.animator = ModelPreviewer.this.animator;
		}
		
		@Override
		public void initAnimator(Animator animator) {
			this.animator = animator;
		}
		
		@Override
		public void updateMotion(boolean considerInaction) {
			
		}

        @Nullable
		@Override
		public AssetAccessor<? extends StaticAnimation> getHitAnimation(StunType stunType) {
			return null;
		}
		
		@Override
		public boolean isLogicalClient() {
			return true;
		}
		
		@Override
		public void cancelItemUse() {
		}
		
		@Override
		public float getAttackDirectionPitch(float partialTick) {
			return 0.0F;
		}
		
		@Override
		public OpenMatrix4f getModelMatrix(float partialTicks) {
			return MathUtils.getModelMatrixIntegral(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, partialTicks, 1.0F, 1.0F, 1.0F);
		}
		
		@Override
		public void poseTick(DynamicAnimation animation, Pose pose, float time, float partialTicks) {
		}
		
		@Override
		public void updateEntityState() {
		}
		
		@Override
		public boolean invalid() {
			return false;
		}
		
		@Override
		public Vec3 getObjectVelocity() {
			return Vec3.ZERO;
		}
		
		@Override
		public Vec3 getAccurateCloakLocation(float partialFrame) {
			return Vec3.ZERO;
		}
		
		@Override
		public Vec3 getAccuratePartialLocation(float partialFrame) {
			return Vec3.ZERO;
		}
		
		@Override
		public float getAccurateYRot(float partialFrame) {
			return ModelPreviewer.this.yRot;
		}
		
		@Override
		public float getYRotDelta(float partialFrame) {
			return ModelPreviewer.this.yRot - ModelPreviewer.this.yRotO;
		}
		
		@Override
		public float getYRot() {
			return ModelPreviewer.this.yRot;
		}
		
		@Override
		public float getYRotO() {
			return ModelPreviewer.this.yRotO;
		}
		
		@Override
		public <SIM extends PhysicsSimulator<?, ?, ?, ?, ?>> Optional<SIM> getSimulator(SimulationTypes<?, ?, ?, ?, ?, SIM> simulationType) {
			return Optional.empty();
		}

		@Override
		public float getScale() {
			return 1.0F;
		}

		@Override
		public Animator getSimulatableAnimator() {
			return this.animator;
		}

		@Override
		public float getGravity() {
			return 9.8F;
		}

        @Nullable
		@Override
		public ClothSimulator getClothSimulator() {
			return null;
		}

        @Nullable
		@Override
		public Faction getFaction() {
			return null;
		}

		@Override
		public boolean isFakeEntity() {
			return true;
		}
	}
	
	public class NoEntityAnimator extends ClientAnimator {
		public NoEntityAnimator(LivingEntityPatch<?> entitypatch) {
			super(entitypatch, NoEntityBaseLayer::new);
		}
		
		@Override
		public void tick() {
			this.baseLayer.update(this.entitypatch);
			
			if (this.baseLayer.animationPlayer.isEnd() && this.baseLayer.getNextAnimation() == null) {
				AssetAccessor<? extends StaticAnimation> toPlay = index > -1 && index < animationsToPlay.size() ? animationsToPlay.get(index) : Animations.EMPTY_ANIMATION;
				this.baseLayer.playAnimation(toPlay, this.entitypatch, 0.0F);
				
				if (!trailInfoList.isEmpty()) {
					for (TrailInfo trailInfo : trailInfoList) {
						if (trailInfo.playable()) {
							CustomTrailParticle trail = new CustomTrailParticle(entitypatch.getArmature().searchJointByName(trailInfo.joint()), toPlay, trailInfo);
							trailParticles.add(trail);
						} else {
							toPlay.get().getProperty(ClientAnimationProperties.TRAIL_EFFECT).ifPresent(trailInfos -> {
								for (TrailInfo info : trailInfos) {
									if (info.hand() != InteractionHand.MAIN_HAND) {
										continue;
									}
									
									TrailInfo combinedTrailInfo = trailInfo.overwrite(info);
									
									if (combinedTrailInfo.playable()) {
										CustomTrailParticle trail = new CustomTrailParticle(entitypatch.getArmature().searchJointByName(combinedTrailInfo.joint()), toPlay, combinedTrailInfo);
										trailParticles.add(trail);
									}
								}
							});
						}
					}
				}
				
				index = animationsToPlay.isEmpty() ? 0 : (index + 1) % animationsToPlay.size();
			}
		}

        @Nullable
		public LivingEntityPatch<?> getEntityPatch() {
			return this.entitypatch;
		}
		
		static class NoEntityAnimationPlayer extends AnimationPlayer {
			@Override
			public void tick(LivingEntityPatch<?> entitypatch) {
				DynamicAnimation nowPlay = this.getAnimation().get();
				
				this.prevElapsedTime = this.elapsedTime;
				this.elapsedTime += EpicFightSharedConstants.A_TICK * (this.isReversed() && nowPlay.canBePlayedReverse() ? -1.0F : 1.0F);
				
				if (this.elapsedTime >= nowPlay.getTotalTime()) {
					if (nowPlay.isRepeat()) {
						this.prevElapsedTime = 0;
						this.elapsedTime %= nowPlay.getTotalTime();
					} else {
						this.elapsedTime = nowPlay.getTotalTime();
						this.isEnd = true;
					}
				} else if (this.elapsedTime < 0) {
					if (nowPlay.isRepeat()) {
						this.prevElapsedTime = nowPlay.getTotalTime();
						this.elapsedTime = nowPlay.getTotalTime() + this.elapsedTime;
					} else {
						this.elapsedTime = 0.0F;
						this.isEnd = true;
					}
				}
			}
			
			@Override
			public void begin(AssetAccessor<? extends DynamicAnimation> animation, LivingEntityPatch<?> entitypatch) {
			}
			
			@Override
			public Pose getCurrentPose(LivingEntityPatch<?> entitypatch, float partialTicks) {
				return this.play.get().getRawPose(this.prevElapsedTime + (this.elapsedTime - this.prevElapsedTime) * partialTicks);
			}
		}
		
		static class NoEntityLayer extends Layer {
			public NoEntityLayer(Priority priority) {
				super(priority, NoEntityAnimationPlayer::new);
			}
			
			public void playAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, float convertTimeModifier) {
				Pose lastPose = entitypatch.getAnimator().getPose(1.0F);
				this.resume();
				
				if (!nextAnimation.get().isMetaAnimation()) {
					this.setLinkAnimation(nextAnimation, entitypatch, lastPose, convertTimeModifier);
					this.linkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
					this.nextAnimation = nextAnimation;
				}
			}
			
			@Override
			public void playAnimationInstantly(AssetAccessor<? extends DynamicAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
				this.resume();
				nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
				this.nextAnimation = null;
			}
			
			@Override
			protected void setLinkAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, Pose lastPose, float convertTimeModifier) {
				Pose currentPose = this.animationPlayer.getAnimation().get().getRawPose(this.animationPlayer.getElapsedTime());
				Pose nextAnimationPose = nextAnimation.get().getRawPose(0.0F);
				float totalTime = nextAnimation.get().getTransitionTime();
				
				AssetAccessor<? extends DynamicAnimation> fromAnimation = this.animationPlayer.isEmpty() ? entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation() : this.animationPlayer.getAnimation();
				
				if (fromAnimation instanceof LinkAnimation fromLinkAnimation) {
					fromAnimation = fromLinkAnimation.getFromAnimation();
				}
				
				this.linkAnimation.getAnimationClip().reset();
				this.linkAnimation.setTotalTime(totalTime);
				this.linkAnimation.setConnectedAnimations(fromAnimation, nextAnimation);
				
				Map<String, JointTransform> data1 = currentPose.getJointTransformData();
				Map<String, JointTransform> data2 = nextAnimationPose.getJointTransformData();
				
				for (String jointName : data1.keySet()) {
					if (data1.containsKey(jointName) && data2.containsKey(jointName)) {
						Keyframe[] keyframes = new Keyframe[2];
						keyframes[0] = new Keyframe(0.0F, data1.get(jointName));
						keyframes[1] = new Keyframe(totalTime, data2.get(jointName));
						TransformSheet sheet = new TransformSheet(keyframes);
						this.linkAnimation.getAnimationClip().addJointTransform(jointName, sheet);
					}
				}
				
				this.animationPlayer.setPlayAnimation(this.linkAnimation);
			}
			
			public void update(LivingEntityPatch<?> entitypatch) {
				if (this.paused) {
					this.animationPlayer.setElapsedTime(this.animationPlayer.getElapsedTime());
				} else {
					this.animationPlayer.tick(entitypatch);
				}
				
				if (!this.paused && this.animationPlayer.isEnd()) {
					if (this.nextAnimation != null) {
						this.nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
						this.nextAnimation = null;
					} else {
						if (this.animationPlayer.getAnimation() instanceof LayerOffAnimation) {
							this.animationPlayer.getAnimation().get().end(entitypatch, Animations.EMPTY_ANIMATION, true);
						} else {
							this.off(entitypatch);
						}
					}
				}
			}
			
			public Pose getEnabledPose(LivingEntityPatch<?> entitypatch, float partialTick) {
				DynamicAnimation animation = this.animationPlayer.getAnimation().get();
				Pose pose = animation.getRawPose(this.animationPlayer.getPrevElapsedTime() + (this.animationPlayer.getElapsedTime() - this.animationPlayer.getPrevElapsedTime()) * partialTick);
				pose.disableJoint(entry -> !animation.hasTransformFor(entry.getKey()));
				
				return pose;
			}
			
			public void off(LivingEntityPatch<?> entitypatch) {
				if (!this.isDisabled() && !(this.animationPlayer.getAnimation() instanceof LayerOffAnimation)) {
					float convertTime = entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation().get().getTransitionTime();
					setLayerOffAnimation(this.animationPlayer.getAnimation(), this.getEnabledPose(entitypatch, 1.0F), this.layerOffAnimation, convertTime);
					this.playAnimationInstantly(this.layerOffAnimation, entitypatch);
				}
			}
		}
		
		static class NoEntityBaseLayer extends Layer.BaseLayer {
			public NoEntityBaseLayer() {
				super(NoEntityAnimationPlayer::new);
				
				this.compositeLayers.clear();
				
				for (Priority priority : Priority.values()) {
					this.compositeLayers.computeIfAbsent(priority, NoEntityLayer::new);
				}
				
				this.baseLayerPriority = Priority.LOWEST;
			}
			
			@Override
			public void playAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, float convertTimeModifier) {
                this.baseLayerPriority = nextAnimation.get().getPriority();
				this.offCompositeLayersLowerThan(entitypatch, nextAnimation);
				
				Pose lastPose = entitypatch.getAnimator().getPose(1.0F);
				this.resume();
				
				if (!nextAnimation.get().isMetaAnimation()) {
					this.setLinkAnimation(nextAnimation, entitypatch, lastPose, convertTimeModifier);
					this.linkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
					entitypatch.updateEntityState();
					this.nextAnimation = nextAnimation;
				}
			}
			
			@Override
			public void playAnimationInstantly(AssetAccessor<? extends DynamicAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
				this.resume();
				nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
				this.nextAnimation = null;
			}
			
			@Override
			protected void playLivingAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch) {
				this.resume();
				
				if (!nextAnimation.get().isMetaAnimation()) {
					this.concurrentLinkAnimation.acceptFrom(this.animationPlayer.getAnimation().get().getRealAnimation(), nextAnimation, this.animationPlayer.getElapsedTime());
					this.concurrentLinkAnimation.putOnPlayer(this.animationPlayer, entitypatch);
					this.nextAnimation = nextAnimation;
				}
			}
			
			@Override
			public void update(LivingEntityPatch<?> entitypatch) {
				if (this.paused) {
					this.animationPlayer.setElapsedTime(this.animationPlayer.getElapsedTime());
				} else {
					this.animationPlayer.tick(entitypatch);
				}
				
				if (!this.paused && this.animationPlayer.isEnd()) {
					if (this.nextAnimation != null) {
						this.nextAnimation.get().putOnPlayer(this.animationPlayer, entitypatch);
						this.nextAnimation = null;
					} else {
						if (this.animationPlayer.getAnimation() instanceof LayerOffAnimation) {
							this.animationPlayer.getAnimation().get().end(entitypatch, Animations.EMPTY_ANIMATION, true);
						} else {
							this.off(entitypatch);
						}
					}
				}
				
				for (Layer layer : this.compositeLayers.values()) {
					layer.update(entitypatch);
				}
			}
			
			@Override
			protected void setLinkAnimation(AssetAccessor<? extends StaticAnimation> nextAnimation, LivingEntityPatch<?> entitypatch, Pose lastPose, float convertTimeModifier) {
				Pose currentPose = this.animationPlayer.getAnimation().get().getRawPose(this.animationPlayer.getElapsedTime());
				Pose nextAnimationPose = nextAnimation.get().getRawPose(0.0F);
				float totalTime = nextAnimation.get().getTransitionTime();
				
				AssetAccessor<? extends DynamicAnimation> fromAnimation = this.animationPlayer.isEmpty() ? entitypatch.getClientAnimator().baseLayer.animationPlayer.getAnimation() : this.animationPlayer.getAnimation();
				
				if (fromAnimation instanceof LinkAnimation fromLinkAnimation) {
					fromAnimation = fromLinkAnimation.getFromAnimation();
				}
				
				this.linkAnimation.getAnimationClip().reset();
				this.linkAnimation.setTotalTime(totalTime);
				this.linkAnimation.setConnectedAnimations(fromAnimation, nextAnimation);
				
				Map<String, JointTransform> data1 = currentPose.getJointTransformData();
				Map<String, JointTransform> data2 = nextAnimationPose.getJointTransformData();
				
				for (String jointName : data1.keySet()) {
					if (data1.containsKey(jointName) && data2.containsKey(jointName)) {
						Keyframe[] keyframes = new Keyframe[2];
						keyframes[0] = new Keyframe(0.0F, data1.get(jointName));
						keyframes[1] = new Keyframe(totalTime, data2.get(jointName));
						TransformSheet sheet = new TransformSheet(keyframes);
						this.linkAnimation.getAnimationClip().addJointTransform(jointName, sheet);
					}
				}
				
				this.animationPlayer.setPlayAnimation(this.linkAnimation);
			}
			
			public void offCompositeLayerLowerThan(LivingEntityPatch<?> entitypatch, StaticAnimation nextAnimation) {
				for (Priority p : nextAnimation.getPriority().lowersAndEqual()) {
					if (p == Priority.LOWEST && !nextAnimation.isMainFrameAnimation()) {
						continue;
					}
					
					this.compositeLayers.get(p).off(entitypatch);
				}
			}

            @Nullable
			public Layer getLayer(Priority priority) {
				return this.compositeLayers.get(priority);
			}
		}
	}
	
	
	class CustomTrailParticle extends AnimationTrailParticle {
        @SuppressWarnings("deprecation")
        protected CustomTrailParticle(Joint joint, AssetAccessor<? extends StaticAnimation> animation, TrailInfo trailInfo) {
            super(entitypatch.getArmature(), entitypatch, joint, animation, trailInfo);
        }

        @Override
        public void tick() {
            AnimationPlayer animPlayer = animator.getPlayerFor(null);
            this.trailEdges.removeIf(v -> !v.isAlive());

            if (this.shouldRemove) {
                if (this.lifetime-- == 0) {
                    this.remove();
                }
            } else {
                if (this.animation != animPlayer.getRealAnimation() || animPlayer.getElapsedTime() > this.trailInfo.endTime()) {
                    this.shouldRemove = true;
                    this.lifetime = this.trailInfo.trailLifetime();
                }
            }

            if (this.trailInfo.fadeTime() > 0.0F && this.trailInfo.endTime() < animPlayer.getElapsedTime()) {
                return;
            }

            boolean isTrailInvisible = animPlayer.getAnimation().get().isLinkAnimation() || animPlayer.getElapsedTime() <= this.trailInfo.startTime();
            boolean isFirstTrail = this.trailEdges.isEmpty();
            boolean needCorrection = (!isTrailInvisible && isFirstTrail);

            if (needCorrection) {
                float startCorrection = Math.max((this.trailInfo.startTime() - animPlayer.getPrevElapsedTime()) / (animPlayer.getElapsedTime() - animPlayer.getPrevElapsedTime()), 0.0F);
                this.startEdgeCorrection = this.trailInfo.interpolateCount() * 2 * startCorrection;
            }

            TrailInfo trailInfo = this.trailInfo;
            Pose prevPose = this.owner.getAnimator().getPose(0.0F);
            Pose middlePose = this.owner.getAnimator().getPose(0.5F);
            Pose currentPose = this.owner.getAnimator().getPose(1.0F);
            OpenMatrix4f prevJointTf = entitypatch.getArmature().getBoundTransformFor(prevPose, this.joint);
            OpenMatrix4f middleJointTf = entitypatch.getArmature().getBoundTransformFor(middlePose, this.joint);
            OpenMatrix4f currentJointTf = entitypatch.getArmature().getBoundTransformFor(currentPose, this.joint);
            Vec3 prevStartPos = OpenMatrix4f.transform(prevJointTf, trailInfo.start());
            Vec3 prevEndPos = OpenMatrix4f.transform(prevJointTf, trailInfo.end());
            Vec3 middleStartPos = OpenMatrix4f.transform(middleJointTf, trailInfo.start());
            Vec3 middleEndPos = OpenMatrix4f.transform(middleJointTf, trailInfo.end());
            Vec3 currentStartPos = OpenMatrix4f.transform(currentJointTf, trailInfo.start());
            Vec3 currentEndPos = OpenMatrix4f.transform(currentJointTf, trailInfo.end());

            List<Vec3> finalStartPositions;
            List<Vec3> finalEndPositions;
            boolean visibleTrail;

            if (isTrailInvisible) {
                finalStartPositions = Lists.newArrayList();
                finalEndPositions = Lists.newArrayList();
                finalStartPositions.add(prevStartPos);
                finalStartPositions.add(middleStartPos);
                finalEndPositions.add(prevEndPos);
                finalEndPositions.add(middleEndPos);

                this.invisibleTrailEdges.clear();
                visibleTrail = false;
            } else {
                List<Vec3> startPosList = Lists.newArrayList();
                List<Vec3> endPosList = Lists.newArrayList();
                TrailEdge edge1;
                TrailEdge edge2;

                if (isFirstTrail) {
                    int lastIdx = this.invisibleTrailEdges.size() - 1;
                    edge1 = this.invisibleTrailEdges.get(lastIdx);
                    edge2 = new TrailEdge(prevStartPos, prevEndPos, -1);
                } else {
                    edge1 = this.trailEdges.get(this.trailEdges.size() - (this.trailInfo.interpolateCount() / 2 + 1));
                    edge2 = this.trailEdges.get(this.trailEdges.size() - 1);
                    edge2.lifetime++;
                }

                startPosList.add(edge1.start);
                endPosList.add(edge1.end);
                startPosList.add(edge2.start);
                endPosList.add(edge2.end);
                startPosList.add(middleStartPos);
                endPosList.add(middleEndPos);
                startPosList.add(currentStartPos);
                endPosList.add(currentEndPos);

                finalStartPositions = CubicBezierCurve.getBezierInterpolatedPoints(startPosList, 1, 3, this.trailInfo.interpolateCount());
                finalEndPositions = CubicBezierCurve.getBezierInterpolatedPoints(endPosList, 1, 3, this.trailInfo.interpolateCount());

                if (!isFirstTrail) {
                    finalStartPositions.remove(0);
                    finalEndPositions.remove(0);
                }

                visibleTrail = true;
            }

            this.makeTrailEdges(finalStartPositions, finalEndPositions, visibleTrail ? this.trailEdges : this.invisibleTrailEdges);
        }

        @Override
        public void render(VertexConsumer vertexConsumer, Camera camera, float partialTick) {
            if (this.trailEdges.isEmpty()) {
                return;
            }


            PoseStack poseStack = new PoseStack();
            this.setupPoseStack(poseStack, camera, partialTick);
            Matrix4f matrix4f = poseStack.last().pose();
            int edges = this.trailEdges.size() - 1;
            boolean startFade = this.trailEdges.get(0).lifetime == 1;
            boolean endFade = this.trailEdges.get(edges).lifetime == this.trailInfo.trailLifetime();
            float startEdge = (startFade ? this.trailInfo.interpolateCount() * 2 * partialTick : 0.0F) + this.startEdgeCorrection;
            float endEdge = endFade ? Math.min(edges - (this.trailInfo.interpolateCount() * 2) * (1.0F - partialTick), edges - 1) : edges - 1;
            float interval = 1.0F / (endEdge - startEdge);
            float fading = 1.0F;

            if (this.shouldRemove) {
                if (TrailInfo.isValidTime(this.trailInfo.fadeTime())) {
                    fading = ((float) this.lifetime / (float) this.trailInfo.trailLifetime());
                } else {
                    fading = Mth.clamp((this.lifetime + (1.0F - partialTick)) / this.trailInfo.trailLifetime(), 0.0F, 1.0F);
                }
            }

            float partialStartEdge = interval * (startEdge % 1.0F);
            float from = -partialStartEdge;
            float to = -partialStartEdge + interval;

            for (int i = (int) (startEdge); i < (int) endEdge + 1; i++) {
                TrailEdge e1 = this.trailEdges.get(i);
                TrailEdge e2 = this.trailEdges.get(i + 1);
                Vector4f pos1 = new Vector4f((float) e1.start.x, (float) e1.start.y, (float) e1.start.z, 1.0F);
                Vector4f pos2 = new Vector4f((float) e1.end.x, (float) e1.end.y, (float) e1.end.z, 1.0F);
                Vector4f pos3 = new Vector4f((float) e2.end.x, (float) e2.end.y, (float) e2.end.z, 1.0F);
                Vector4f pos4 = new Vector4f((float) e2.start.x, (float) e2.start.y, (float) e2.start.z, 1.0F);

                pos1.mul(matrix4f);
                pos2.mul(matrix4f);
                pos3.mul(matrix4f);
                pos4.mul(matrix4f);

                float alphaFrom = Mth.clamp(from, 0.0F, 1.0F);
                float alphaTo = Mth.clamp(to, 0.0F, 1.0F);

                vertexConsumer.addVertex(pos1.x(), pos1.y(), pos1.z()).setUv(from, 1.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).setLight(0);
                vertexConsumer.addVertex(pos2.x(), pos2.y(), pos2.z()).setUv(from, 0.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaFrom * fading).setLight(0);
                vertexConsumer.addVertex(pos3.x(), pos3.y(), pos3.z()).setUv(to, 0.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).setLight(0);
                vertexConsumer.addVertex(pos4.x(), pos4.y(), pos4.z()).setUv(to, 1.0F).setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaTo * fading).setLight(0);

                from += interval;
                to += interval;
            }
        }

        @Override
        protected void setupPoseStack(PoseStack poseStack, Camera camera, float partialTicks) {
            float x = xMove;
            float y = yMove;
            float z = (float) zoom;
            float xRot = ModelPreviewer.this.xRot;
            float yRot = ModelPreviewer.this.yRot;

            poseStack.translate(x, y - 1.0D, z);
            poseStack.mulPose(QuaternionUtils.XP.rotationDegrees(xRot));
            poseStack.mulPose(QuaternionUtils.YP.rotationDegrees(yRot));
        }
    }

    /*******************************************************************
     * {@link AnchoredWidget} implementations                         *
     *******************************************************************/
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private final AnchoredWidget.HorizontalAnchorType horizontalAnchorType;
    private final AnchoredWidget.VerticalAnchorType verticalAnchorType;

    @Override
    public int getX1() {
        return this.x1;
    }

    @Override
    public int getX2() {
        return this.x2;
    }

    @Override
    public int getY1() {
        return this.y1;
    }

    @Override
    public int getY2() {
        return this.y2;
    }

    @Override
    public void setX1(int i) {
        this.x1 = i;
    }

    @Override
    public void setX2(int i) {
        this.x2 = i;
    }

    @Override
    public void setY1(int i) {
        this.y1 = i;
    }

    @Override
    public void setY2(int i) {
        this.y2 = i;
    }

    @Override
    public HorizontalAnchorType getHorizontalAnchorType() {
        return this.horizontalAnchorType;
    }

    @Override
    public VerticalAnchorType getVerticalAnchorType() {
        return this.verticalAnchorType;
    }

    @Override
    public void relocate(ScreenRectangle screenRectangle) {
        AnchoredWidget.super.relocate(screenRectangle);
        this.showColliderCheckbox.relocate(screenRectangle);
        this.showItemCheckbox.relocate(screenRectangle);
        this.showTrailCheckbox.relocate(screenRectangle);

    }

	public void sysoutTranslationInfo() {
		System.out.println("x move: " + this.xMove +" y move: "+ this.yMove +" x rot: "+ this.xRot +" y rot: "+ this.yRot +" zoom: "+ this.zoom);
	}
}
