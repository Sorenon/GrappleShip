package net.sorenon.grappleship.mixin;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Inject(at = @At("RETURN"), method = "getLeftText")
    protected void getLeftText(CallbackInfoReturnable<List<String>> info) {
//        ClientPlayerEntity player = MinecraftClient.getInstance().player;
//        if (player != null) {
//            Vec3d prev_pos = new Vec3d(player.prevX, player.prevY, player.prevZ);
//            Vec3d true_vel = player.getPos().subtract(prev_pos);
//            double speed = true_vel.multiply(1,0,1).length();
//
//            info.getReturnValue().add(String.format("[MODID] Velocity XYZ: %.3f / %.3f / %.3f", true_vel.x, true_vel.y, true_vel.z));
//            info.getReturnValue().add(String.format("[MODID] Hor Speed: %.5f", speed));
//            info.getReturnValue().add(String.format("[MODID] Speed: %.5f", true_vel.length()));
//        }
    }
}
