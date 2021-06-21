package net.fabricmc.example.movement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class AirStrafeMovement extends Movement {

    public double accel = 14;
    public double movement_speed = 0;

    public AirStrafeMovement() {

    }

    public AirStrafeMovement(double movement_speed){
        this.movement_speed = movement_speed;
    }

    @Override
    public Movement travel(LivingEntity entity, Vec3d input, boolean jumping) {
        if (entity.isOnGround() || entity.isTouchingWater() || entity.isInLava() || entity.isFallFlying()) {
            return null;
        }

        Vec3d vel = entity.getVelocity();
        Vec3d wish_dir = inputToWishDir(input.multiply(1, 0, 1), entity.getYaw());

        double movement_speed = this.movement_speed;
        if (movement_speed == 0) {
            movement_speed = entity.flyingSpeed;
        }

        double wish_speed = movement_speed * wish_dir.length();

        Vec3d horizontal_vel = vel.multiply(1, 0, 1);

        horizontal_vel = Accelerate(horizontal_vel, wish_dir.normalize(), wish_speed, accel);

        vel = horizontal_vel.add(0, vel.y, 0);

        entity.setVelocity(vel.x, (vel.y - 0.08) * 0.9800000190734863D, vel.z);

        entity.move(net.minecraft.entity.MovementType.SELF, vel);
//        if (entity.verticalCollision && entity.isOnGround()) {
//            vel = vel.multiply(1, 0, 1);
//        }
//
//        entity.setVelocity(vel.x, (vel.y - 0.08) * 0.9800000190734863D, vel.z);

        return this;
    }

    private Vec3d Accelerate(Vec3d velocity, Vec3d wish_dir, double wish_speed, double accel) {
        double current_speed = velocity.dotProduct(wish_dir);
        double add_speed = wish_speed - current_speed;

        if (add_speed <= 0) {
            return velocity;
        }

        /*m_surfaceFriction*/
        double accel_speed = Math.min(
                wish_speed * (accel * 1 / 20),
                add_speed);

        return velocity.add(wish_dir.multiply(accel_speed, 0, accel_speed));
    }
}
