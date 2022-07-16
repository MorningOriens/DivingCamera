package zashine.divingcamera;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

@Mod(DivingCamera.MOD_ID)
public class DivingCamera {

    public static final String MOD_ID = "divingcamera";
    public float lastRool;
    public float rool;
    public float mouseRool;
    public float mouseRoolState;
    public float mouseRoolStateRender;
    public long mouseResetTime;
    public float inputRool;
    public float inputRoolState;
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
    public void onSetupCamera(EntityViewRenderEvent.CameraSetup event) {
        if(Minecraft.getInstance().player != null)
        if(Minecraft.getInstance().player.isFallFlying()) {
            event.setRoll((float) (this.lastRool + (this.rool - this.lastRool) * event.getPartialTicks()));
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) return;
        final float mouseSensitive = 0.4f;
        this.lastRool = this.rool;
        this.inputRoolState += this.inputRool;
        final int length = 400;
        final int smoothLength = 100;
        if (Mth.abs(this.mouseRool) > mouseSensitive) {
            if (this.mouseRoolState * this.mouseRool < 0.0f) this.mouseRoolState = this.mouseRool;
            else this.mouseRoolState += this.mouseRool;
            this.mouseResetTime = System.currentTimeMillis() + length;
            this.mouseRool = 0.0f;
        }
        if (Mth.abs(this.inputRoolState) < 2.0f) this.inputRoolState = 0.0f;
        else if (this.inputRoolState > 0.0f) this.inputRoolState -= 2.0f;
        else this.inputRoolState += 2.0f;
        if (Mth.abs(this.mouseRoolState) > mouseSensitive && System.currentTimeMillis() > this.mouseResetTime - (length - smoothLength)) {
            float offset = Mth.clamp((float)((System.currentTimeMillis() - (this.mouseResetTime - (length - smoothLength))) / (length - smoothLength)) * (Mth.abs(this.mouseRoolState) / 30.0f), 0.0f, 1.0f) * 3.0f;
            if (Mth.abs(this.mouseRoolState) < offset) this.mouseRoolState = 0.0f;
            else if (this.mouseRoolState > 0.0f) this.mouseRoolState -= offset;
            else this.mouseRoolState += offset;
        }
        float renderOffset = 4.0f * 3.0f * Mth.abs(this.mouseRoolStateRender - this.mouseRoolState) / 60.0f;
        if (Mth.abs(this.mouseRoolStateRender - this.mouseRoolState) < renderOffset) this.mouseRoolStateRender = this.mouseRoolState;
        else if (this.mouseRoolStateRender > this.mouseRoolState) this.mouseRoolStateRender -= renderOffset;
        else this.mouseRoolStateRender += renderOffset;
        this.rool = Mth.clamp(this.inputRoolState, -10.0f, 10.0f) + Mth.clamp(this.mouseRoolState, -30.0f, 30.0f);
    }

    @SubscribeEvent
    public void onInput(MovementInputUpdateEvent event) {
        this.inputRool = -3.0f * event.getInput().leftImpulse;
    }

    public static DivingCamera getInstance() {
        return instance;
    }
}
