package zashine.divingcamera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod(DivingCamera.MOD_ID)
public class DivingCamera {

    public static final String MOD_ID = "divingcamera";

    public float lastRoll;
    public float roll;
    public float mouseRoll;
    public float mouseRollState;
    public float mouseRollStateRender;
    public long mouseResetTime;
    public float inputRoll;
    public float inputRollState;
    public ArrayList<Float> mouseInputs = new ArrayList<>() {
        {
            for (int i = 0; i < 10; ++i) {
                this.add(0.0f);
            }
        }
    };

    private static DivingCamera instance;

    public DivingCamera() {
        instance = this;
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () ->  () -> MinecraftForge.EVENT_BUS.register(DivingCamera.this));
    }

    @SubscribeEvent
    public void onSetupCamera(ViewportEvent.ComputeCameraAngles event) {
        if(Minecraft.getInstance().player != null)
        if(Minecraft.getInstance().player.isFallFlying()) {
            event.setRoll(Mth.lerp((float) event.getPartialTick(), this.lastRoll, this.roll));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        final float mouseSensitive = 0.4f;
        this.lastRoll = this.roll;
        this.inputRollState += this.inputRoll;
        final int length = 400;
        final int smoothLength = 100;
        if (Mth.abs(this.mouseRoll) > mouseSensitive) {
            if (this.mouseRollState * this.mouseRoll < 0.0f) this.mouseRollState = this.mouseRoll;
            else this.mouseRollState += this.mouseRoll;
            this.mouseResetTime = System.currentTimeMillis() + length;
            this.mouseRoll = 0.0f;
        }
        if (Mth.abs(this.inputRollState) < 2.0f) this.inputRollState = 0.0f;
        else if (this.inputRollState > 0.0f) this.inputRollState -= 2.0f;
        else this.inputRollState += 2.0f;
        if (Mth.abs(this.mouseRollState) > mouseSensitive && System.currentTimeMillis() > this.mouseResetTime - (length - smoothLength)) {
            float offset = Mth.clamp((float)((System.currentTimeMillis() - (this.mouseResetTime - (length - smoothLength))) / (length - smoothLength)) * (Mth.abs(this.mouseRollState) / 30.0f), 0.0f, 1.0f) * 3.0f;
            if (Mth.abs(this.mouseRollState) < offset) this.mouseRollState = 0.0f;
            else if (this.mouseRollState > 0.0f) this.mouseRollState -= offset;
            else this.mouseRollState += offset;
        }
        float renderOffset = 4.0f * 3.0f * Mth.abs(this.mouseRollStateRender - this.mouseRollState) / 60.0f;
        if (Mth.abs(this.mouseRollStateRender - this.mouseRollState) < renderOffset) this.mouseRollStateRender = this.mouseRollState;
        else if (this.mouseRollStateRender > this.mouseRollState) this.mouseRollStateRender -= renderOffset;
        else this.mouseRollStateRender += renderOffset;
        this.mouseRollState = Mth.clamp(this.mouseRollState, -30.0f, 30.0f);
        this.inputRollState = Mth.clamp(this.inputRollState, -10.0f, 10.0f);
        this.roll = mouseRollStateRender + inputRollState;
    }

    @SubscribeEvent
    public void onInput(MovementInputUpdateEvent event) {
        this.inputRoll = -3.0f * event.getInput().leftImpulse;
    }

    public static DivingCamera getInstance() {
        return instance;
    }
}
