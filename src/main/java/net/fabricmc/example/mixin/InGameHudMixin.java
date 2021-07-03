package net.fabricmc.example.mixin;

import com.google.common.base.Strings;
import net.fabricmc.example.accessors.LivingEntityExt;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.minecraft.client.gui.DrawableHelper.fill;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Shadow
    public abstract TextRenderer getTextRenderer();

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
}
