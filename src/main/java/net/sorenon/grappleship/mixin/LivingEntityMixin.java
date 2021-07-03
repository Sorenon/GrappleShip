package net.sorenon.grappleship.mixin;

import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.movement.AirStrafeMovement;
import net.sorenon.grappleship.movement.Movement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.EntityDamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements LivingEntityExt {

    @Shadow
    public abstract boolean canMoveVoluntarily();

    @Shadow protected boolean jumping;

    @Shadow public abstract void updateLimbs(LivingEntity entity, boolean flutter);

    @Shadow public abstract ItemStack getMainHandStack();

    @Shadow private float absorptionAmount;

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

    @ModifyVariable(method = "damage", ordinal = 0, at = @At("HEAD"))
    float modDamageA(float amount, DamageSource damageSource) {
        if (damageSource.getAttacker() instanceof LivingEntityMixin attacker) {
            if (attacker.movement instanceof AirStrafeMovement && attacker.getMainHandStack().getItem() instanceof ShovelItem) {
                return amount * 4;
            }
        }
        return amount;
    }

    @ModifyVariable(method = "damage", ordinal = 0, at = @At("HEAD"))
    DamageSource modDamageB(DamageSource damageSource) {
        if (damageSource.getAttacker() instanceof LivingEntityMixin attacker) {
            if (attacker.movement instanceof AirStrafeMovement && attacker.getMainHandStack().getItem() instanceof ShovelItem) {
                return new EntityDamageSource("grappleship.market_garden", attacker);
            }
        }
        return damageSource;
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
