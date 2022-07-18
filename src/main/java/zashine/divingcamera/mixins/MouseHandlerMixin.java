package zashine.divingcamera.mixins;

import net.minecraft.client.MouseHandler;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zashine.divingcamera.DivingCamera;

import static org.spongepowered.asm.mixin.injection.callback.LocalCapture.CAPTURE_FAILSOFT;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {

    @Inject(
        method = "turnPlayer",
        at = @At(value = "INVOKE",
        shift = At.Shift.AFTER,
        target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"),
        locals = CAPTURE_FAILSOFT
    )
    public void injectTurnPlayer(CallbackInfo ci, double d0, double d1, double d4, double d5, double d6, double d2) {
        DivingCamera divingCamera = DivingCamera.getInstance();
        divingCamera.mouseInputs.remove(0);
        final float mouseSensitive = 0.2f;
        final float mouseInput = (float)d2 * 0.35f;
        divingCamera.mouseInputs.add(Mth.abs(mouseInput) > mouseSensitive ? mouseInput : 0.0F);
        float result = 0.0f;
        for (int i = 0; i < divingCamera.mouseInputs.size(); ++i) {
            result += (float)(0.018181818182 * (i + 1) * divingCamera.mouseInputs.get(i));
        }
        divingCamera.mouseRoll = result;
    }

}
