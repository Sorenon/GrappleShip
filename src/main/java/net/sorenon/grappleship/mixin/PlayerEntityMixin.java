package net.sorenon.grappleship.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.sorenon.grappleship.worldshell.GhastAirShip;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "shouldDismount", cancellable = true, at = @At("HEAD"))
    void cancelDismount(CallbackInfoReturnable<Boolean> cir) {
        if (this.getVehicle() instanceof GhastAirShip) {
            cir.setReturnValue(false);
        }
    }
}
