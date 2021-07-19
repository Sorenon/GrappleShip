package net.sorenon.grappleship.items;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.sorenon.grappleship.GrappleshipMod;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.movement.GrappleHookMovement;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.sorenon.grappleship.worldshell.GhastAirShip;

import java.util.function.Predicate;

public class WristGrappleItem extends Item {
    public WristGrappleItem(Settings settings) {
        super(settings);
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    public UseAction getUseAction(ItemStack stack) {
        return UseAction.NONE;
    }

    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            if (((LivingEntityExt) user).getMovement() instanceof GrappleHookMovement) {
                return new TypedActionResult<>(ActionResult.PASS, user.getStackInHand(hand));
            }

            HitResult res = raycast(user, user.getStackInHand(hand));

            if (res instanceof EntityHitResult eRes) {
                var buf = PacketByteBufs.create();
                Vec3d pos = eRes.getPos().subtract(eRes.getEntity().getPos());
                buf.writeDouble(pos.x);
                buf.writeDouble(pos.y);
                buf.writeDouble(pos.z);
                buf.writeInt(eRes.getEntity().getId());
                ClientPlayNetworking.send(GrappleshipMod.C2S_START_GRAPPLE, buf);
                GrappleHookMovement.start(user, pos, eRes.getEntity());
            } else if (res.getType() == HitResult.Type.BLOCK) {
                var buf = PacketByteBufs.create();
                buf.writeDouble(res.getPos().x);
                buf.writeDouble(res.getPos().y);
                buf.writeDouble(res.getPos().z);
                buf.writeInt(0);
                ClientPlayNetworking.send(GrappleshipMod.C2S_START_GRAPPLE, buf);
                GrappleHookMovement.start(user, res.getPos(), null);
            }

            if (res.getType() != HitResult.Type.MISS) {
                user.getAbilities().flying = false;

                user.setCurrentHand(hand);
                return new TypedActionResult<>(ActionResult.SUCCESS, user.getStackInHand(hand));
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (world.isClient) {
            var ext = ((LivingEntityExt) user);
            if (ext.getMovement() instanceof GrappleHookMovement movement) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(false);
                ClientPlayNetworking.send(GrappleshipMod.C2S_END_GRAPPLE, buf);
                movement.end(user, false);
            }
        }
    }

    public double getLength(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("Length", NbtType.DOUBLE)) {
            tag.putDouble("Length", 10);
        }
        return tag.getDouble("Length");
    }

    public boolean likesBlocks(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("LikesBlocks", NbtType.BYTE)) {
            tag.putBoolean("LikesBlocks", true);
        }
        return tag.getBoolean("LikesBlocks");
    }

    public boolean likesEntities(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("LikesEntities", NbtType.BYTE)) {
            tag.putBoolean("LikesEntities", false);
        }
        return tag.getBoolean("LikesEntities");
    }

    public double getSpeed(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("Speed", NbtType.DOUBLE)) {
            tag.putDouble("Speed", 0.05 * 3);
        }
        return tag.getDouble("Speed");
    }

    public double getHandling(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("Handling", NbtType.DOUBLE)) {
            tag.putDouble("Handling", 0.5);
        }
        return tag.getDouble("Handling");
    }

    public double getDamping(ItemStack stack) {
        var tag = stack.getOrCreateTag();
        if (!tag.contains("Damping", NbtType.DOUBLE)) {
            tag.putDouble("Damping", 0.8);
        }
        return tag.getDouble("Damping");
    }

    public HitResult raycast(LivingEntity entity, ItemStack stack) {
        return raycast(entity, getLength(stack), likesBlocks(stack), likesEntities(stack));
    }

    public HitResult raycast(LivingEntity user, double distance, boolean likesBlocks, boolean likesEntities) {
        Vec3d start = user.getCameraPosVec(1.0f);
        Vec3d look = user.getRotationVec(1.0f);
        Vec3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
        HitResult res = user.world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));

        double distanceSqr = distance * distance;
        if (res != null) {
            distanceSqr = res.getPos().squaredDistanceTo(start);
        }

        Box box = user.getBoundingBox().stretch(look.multiply(distance)).expand(1.0D, 1.0D, 1.0D);
        Predicate<Entity> predicate = (entityx) -> !entityx.isSpectator() && entityx.collides();
        if (likesEntities) {
            predicate = (entityx) -> entityx instanceof GhastAirShip;
        }

        EntityHitResult entityHitResult = ProjectileUtil.raycast(user, start, end, box, predicate, distanceSqr);

        if (entityHitResult != null && entityHitResult.getPos().squaredDistanceTo(start) < distanceSqr) {
            res = entityHitResult;
        }


        if (!likesBlocks && res instanceof BlockHitResult) {
            return BlockHitResult.createMissed(Vec3d.ZERO, Direction.DOWN, BlockPos.ORIGIN);
        }
        return res;
    }
}
