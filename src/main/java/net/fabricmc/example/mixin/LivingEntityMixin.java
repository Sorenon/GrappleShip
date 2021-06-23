package net.fabricmc.example.mixin;

import net.fabricmc.example.accessors.LivingEntityExt;
import net.fabricmc.example.movement.Movement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExt {

    @Shadow
    public abstract boolean canMoveVoluntarily();

    @Shadow protected boolean jumping;

    @Shadow public abstract void updateLimbs(LivingEntity entity, boolean flutter);

    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private Movement movement = null;

    @Unique
    private Movement wantedGrappleMovement = null;
    private int grappleTicksLeft = 0;

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    void travel(Vec3d movementInput, CallbackInfo ci) {
        if (this.canMoveVoluntarily() || this.isLogicalSideForUpdatingMovement()) {
            if (movement != null) {
                movement = movement.travel((LivingEntity) (Object)this, movementInput, jumping);
                if (movement != null) {
                    this.updateLimbs((LivingEntity) (Object)this, this instanceof Flutterer);
                    ci.cancel();
                }
            }
        }
    }

    @Override
    public Movement getMovement() {
        return movement;
    }

    @Override
    public void setMovement(Movement movement) {
        this.movement = movement;
    }

    @Override
    public Movement getWantedGrappleMovement() {
        return wantedGrappleMovement;
    }

    @Override
    public void setWantedGrappleMovement(Movement movement) {
        this.wantedGrappleMovement = movement;
    }

    @Override
    public int getGrappleTicks() {
        return grappleTicksLeft;
    }

    @Override
    public void setGrappleTicks(int ticks) {
        this.grappleTicksLeft = ticks;
    }
}
