package net.sorenon.grappleship.movement;

import com.badlogic.gdx.physics.bullet.collision._btMprSimplex_t;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.sorenon.grappleship.GrappleshipMod;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.items.GrappleHookItem;
import net.sorenon.grappleship.items.WristGrappleItem;
import net.sorenon.grappleship.mixin.LivingEntityAcc;
import net.sorenon.grappleship.mixin.ServerPlayNetworkHandlerAcc;
import org.jetbrains.annotations.Nullable;

public class GrappleHookMovement extends Movement {

    private final Entity entityTarget;
    private final Vec3d hitPos;

    public boolean hasNotJumped = false;

    public double damping = 0.8;
    public double handling = 0.5;
    public double speed = 0.05 * 3;

    public GrappleHookMovement(Vec3d hit) {
        this.entityTarget = null;
        this.hitPos = hit;
    }

    public GrappleHookMovement(Entity entityTarget, Vec3d hit) {
        this.entityTarget = entityTarget;
        this.hitPos = hit;
    }

    public Vec3d getTarget() {
        if (entityTarget == null) {
            return hitPos;
        } else {
            return entityTarget.getPos().add(hitPos);
        }
    }

    @Override
    public Movement travel(LivingEntity entity, Vec3d input, boolean jumping) {
        if (entity instanceof ServerPlayerEntity player) {
            ((ServerPlayNetworkHandlerAcc) player.networkHandler).setFloatingTicks(0);
        }
        else if (entity.world.isClient) {
            if (!(entity.getActiveItem().getItem() instanceof WristGrappleItem)) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(false);
                ClientPlayNetworking.send(GrappleshipMod.C2S_END_GRAPPLE, buf);

                return end(entity, false).travel(entity, input, jumping);
            }

            if (jumping && hasNotJumped) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(true);
                ClientPlayNetworking.send(GrappleshipMod.C2S_END_GRAPPLE, buf);

                return end(entity, true).travel(entity, input, jumping);
            }
        }

        if (!jumping) {
            hasNotJumped = true;
        }

        Vec3d delta = getTarget().subtract(entity.getPos().add(0, entity.getHeight() / 2, 0));
//                if (true) {
//                    Vec3d dir = delta.normalize();
//
//                    Vec3d vel = dir.multiply(14f * 1/20);
//                    this.move(MovementType.SELF, vel);
//                    this.setVelocity(vel);
//
//                    ci.cancel();
//                    return;
//                }

        if (GrappleHookItem.mode == GrappleHookItem.Mode.DOT_3D) {
            if (entity instanceof PlayerEntity player) {
                player.getAbilities().flying = false;
            }
//					Vec3d dir = delta.normalize();
//
//					double damping = 0.94;
//					double gravity = -0.08 * 0.1 * 0;
//					//.multiply(damping).add(0, gravity, 0)
////                        if (world.isClient) {
////                            System.out.println("~~~~~~~~~~~~~~~~~~~~");
////                            System.out.println(getVelocity());
////                            System.out.println(getVelocity().multiply(damping));
////                            System.out.println(getVelocity().normalize().multiply(getVelocity().length() * damping));
////                        }
//
//					Vec3d velIn = getVelocity().multiply(damping).add(0, gravity, 0);
//
//					float wishSpeed = (float) Math.min(14f * 1 / 20, delta.length());
//
//					Vec3d velOut = Accelerate3D(velIn, dir, wishSpeed, 8.f * 1 / 20);
//
//					this.move(MovementType.SELF, velOut);
//
//					this.setVelocity(velOut);

            Vec3d dir = delta.normalize();
            Vec3d addVel;
            double damping = this.damping;
//            double damping = 0.85;
            Vec3d wishDir = inputToWishDir(input, entity.getYaw()); //TODO tilt wish dir
//                dir = wishDir.add(dir.multiply(1.6)).normalize();
            dir = wishDir.add(dir.multiply(1 + handling)).normalize();

            addVel = dir.multiply(speed);
            if (delta.length() < 1) {
                damping = 0.25;
            }

            Vec3d vel = entity.getVelocity();
            vel = vel.multiply(damping);
            vel = vel.add(addVel);

            entity.setVelocity(vel);
            entity.fallDistance = 0;
            entity.move(net.minecraft.entity.MovementType.SELF, vel);

        } else if (GrappleHookItem.mode == GrappleHookItem.Mode.DOT_2D) {
            Vec3d xzDir = delta.multiply(1, 0, 1).normalize();
            Vec3d xzVel = Accelerate3D(entity.getVelocity().multiply(1, 0, 1), xzDir, 12, 5);
            entity.setVelocity(xzVel.add(0, MathHelper.clamp(delta.y, -0.4f, 0.4f), 0));
            entity.move(net.minecraft.entity.MovementType.SELF, entity.getVelocity());
        }

        return this;
    }

    @Override
    public String name() {
        return "Grappling";
    }

    public void grappleJump(LivingEntity entity) {
        float f = ((LivingEntityAcc) entity).callGetJumpVelocity() * 1.25f;
        if (entity.hasStatusEffect(StatusEffects.JUMP_BOOST)) {
            f += 0.1F * (float) (entity.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1);
        }

        Vec3d vec3d = entity.getVelocity();
        entity.setVelocity(vec3d.x, f, vec3d.z);
    }

    private Vec3d Accelerate3D(Vec3d velocity, Vec3d wish_dir, double wish_speedIn, double accelIn) {
        double wish_speed = wish_speedIn;
        double accel = accelIn;

        double current_speed = velocity.dotProduct(wish_dir);
        double add_speed = wish_speed - current_speed;

        if (add_speed <= 0) {
            return velocity;
        }

        /*m_surfaceFriction*/
        double accel_speed = Math.min(
                wish_speed * accel,
                add_speed);

        return velocity.add(wish_dir.multiply(accel_speed));
    }

    private Vec3d Accelerate2D(Vec3d velocity, Vec3d wish_dir, double wish_speed, double accel) {
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

    public static void start(LivingEntity entity, Vec3d pos, @Nullable Entity entityTarget) {
        if (entity.world.isClient) {
            entity.world.playSound(entity.getX(), entity.getEyeY(), entity.getZ(), SoundEvents.ITEM_SPYGLASS_USE, entity.getSoundCategory(), 1.0f, 1.0f, true);
        }
        if (entity instanceof PlayerEntity player) {
            player.getAbilities().flying = false;
        }

        GrappleHookMovement grappleHookMovement;
        if (entityTarget == null) {
            grappleHookMovement = new GrappleHookMovement(pos);
            ((LivingEntityExt) entity).setMovement(grappleHookMovement);
        } else {
            grappleHookMovement = new GrappleHookMovement(entityTarget, pos);
            ((LivingEntityExt) entity).setMovement(grappleHookMovement);
            if (entityTarget instanceof LivingEntityExt livingEntity && !(entityTarget instanceof PlayerEntity)) {
                livingEntity.setMovement(new FrozenMovement((movement, entity1, input, jumping) -> entity.isRemoved() || !(((LivingEntityExt) entity).getMovement() instanceof GrappleHookMovement)));
            }
        }

        ItemStack stack = entity.getMainHandStack();
        if (!(stack.getItem() instanceof WristGrappleItem)) {
            stack = entity.getOffHandStack();
        }
        if (stack.getItem() instanceof WristGrappleItem item) {
            double speed = item.getSpeed(stack);
            double handling = item.getHandling(stack);
            double damping = item.getDamping(stack);
            grappleHookMovement.speed = speed;
            grappleHookMovement.handling = handling;
            grappleHookMovement.damping = damping;
        }
    }

    public AirStrafeMovement end(LivingEntity entity, boolean jump) {
        LivingEntityExt ext = (LivingEntityExt) entity;
        if (jump) {
            grappleJump(entity);
        }
        if (entity.world.isClient) {
            if (jump) {
                entity.world.playSound(entity.getX(), entity.getEyeY(), entity.getZ(), SoundEvents.BLOCK_CHAIN_BREAK, entity.getSoundCategory(), 1.0f, 1.0f, true);
            } else {
                entity.world.playSound(entity.getX(), entity.getEyeY(), entity.getZ(), SoundEvents.BLOCK_CHAIN_PLACE, entity.getSoundCategory(), 1.0f, 1.0f, true);
            }
        }
        var next = new AirStrafeMovement(0.05);
        ext.setMovement(next);
        return next;
    }
}
