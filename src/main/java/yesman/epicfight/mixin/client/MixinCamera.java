package yesman.epicfight.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.camera.EpicFightCameraAPI;
import yesman.epicfight.api.client.event.types.camera.BuildCameraTransform;

/// Lets Epic Fight build the camera transform. Since 1.21.9 the level and entity are assigned before
/// the camera update, and `Camera#alignWithEntity` is what used to be `Camera#setup`.
@Mixin(value = Camera.class)
public abstract class MixinCamera {
    @Shadow
    private boolean detached;
    
    @Inject(at = @At(value = "HEAD"), method = "alignWithEntity(F)V", cancellable = true)
    public void epicfight$setup(float partialTick, CallbackInfo callbackInfo) {
        EpicFightCameraAPI cameraApi = EpicFightCameraAPI.getInstance();
        Camera camera = (Camera)(Object)this;
        BuildCameraTransform.Pre buildEvent = cameraApi.setupCamera(camera, partialTick);

        if (!buildEvent.isCanceled()) {
            if (buildEvent.isVanillaCameraSetupCanceled()) {
                // Vanilla assigns this while aligning, which is skipped.
                this.detached = !Minecraft.getInstance().options.getCameraType().isFirstPerson();
                callbackInfo.cancel();
            } else {
                cameraApi.fireCameraBuildPost(camera, partialTick);
            }
        }
    }
}
