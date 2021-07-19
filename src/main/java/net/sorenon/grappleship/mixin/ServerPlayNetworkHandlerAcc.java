package net.sorenon.grappleship.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayNetworkHandler.class)
public interface ServerPlayNetworkHandlerAcc {

    @Accessor
    void setFloatingTicks(int ticks);

    @Accessor
    void setVehicleFloatingTicks(int ticks);
}
