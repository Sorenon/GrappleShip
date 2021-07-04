package net.sorenon.grappleship.mixin;

import com.google.common.base.Strings;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.HitResult;
import net.sorenon.grappleship.GrappleshipMod;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract TextRenderer getTextRenderer();

    @Shadow
    private int scaledHeight;

    @Shadow
    private int scaledWidth;

    @Shadow
    protected abstract PlayerEntity getCameraPlayer();

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/util/math/MatrixStack;)V"))
    void render(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (client.player instanceof LivingEntityExt entity && entity.getMovement() != null) {
            String string = entity.getMovement().name();
            if (!Strings.isNullOrEmpty(string)) {
                Objects.requireNonNull(this.getTextRenderer());
                int j = 9;
                int k = this.getTextRenderer().getWidth(string);
                int m = 2 + j * 4;
                fill(matrices, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
                this.getTextRenderer().draw(matrices, string, 2.0F, (float) m, 14737632);
            }
        }
    }

    @Inject(method = "renderCrosshair", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V"))
    void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        ItemStack stack = getCameraPlayer().getMainHandStack();
        if (stack.getItem() != GrappleshipMod.WRIST_GRAPPLE_ITEM) {
            stack = getCameraPlayer().getOffHandStack();
            if (stack.getItem() != GrappleshipMod.WRIST_GRAPPLE_ITEM) {
                return;
            }
        }
        HitResult res = GrappleshipMod.WRIST_GRAPPLE_ITEM.raycast(getCameraPlayer(), stack);
        if (res.getType() != HitResult.Type.MISS) {
            int j = this.scaledHeight / 2 - 7 + 16;
            int k = this.scaledWidth / 2 - 8;
            this.drawTexture(matrices, k, j, 68, 94, 16, 16);
        }
    }
}
