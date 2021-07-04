package net.sorenon.grappleship.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityPassengersSetS2CPacket;
import net.minecraft.text.TranslatableText;
import net.sorenon.grappleship.GrappleshipClient;
import net.sorenon.grappleship.worldshell.GhastAirShip;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onEntityPassengersSet", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;setOverlayMessage(Lnet/minecraft/text/Text;Z)V", shift = At.Shift.AFTER))
    void message(EntityPassengersSetS2CPacket packet, CallbackInfo ci) {
        if (client.player.getVehicle() instanceof GhastAirShip) {
            this.client.inGameHud.setOverlayMessage(new TranslatableText("mount.onboard", GrappleshipClient.keyBinding.getBoundKeyLocalizedText()), false);
        }
    }
}
