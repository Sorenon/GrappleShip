package net.fabricmc.example.movement;

import net.fabricmc.example.mixin.LivingEntityAcc;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class Movement {
    public abstract Movement travel(LivingEntity entity, Vec3d input, boolean jumping);

    public static Vec3d inputToWishDir(Vec3d movementInput, float yaw) {
        double d = movementInput.lengthSquared();
        if (d < 1.0E-7D) {
            return Vec3d.ZERO;
        } else {
            Vec3d vec3d = (d > 1.0D ? movementInput.normalize() : movementInput);
            float f = MathHelper.sin(yaw * 0.017453292F);
            float g = MathHelper.cos(yaw * 0.017453292F);
            return new Vec3d(vec3d.x * (double) g - vec3d.z * (double) f, vec3d.y, vec3d.z * (double) g + vec3d.x * (double) f);
        }
    }

    public abstract String name();

    public static boolean water(LivingEntity entity){
        FluidState fluidState = entity.world.getFluidState(entity.getBlockPos());

        return entity.isTouchingWater() && ((LivingEntityAcc)entity).callShouldSwimInFluids() && !entity.canWalkOnFluid(fluidState.getFluid());
    }

    public static boolean lava(LivingEntity entity){
        FluidState fluidState = entity.world.getFluidState(entity.getBlockPos());

        return entity.isInLava() && ((LivingEntityAcc)entity).callShouldSwimInFluids() && !entity.canWalkOnFluid(fluidState.getFluid());
    }
}
