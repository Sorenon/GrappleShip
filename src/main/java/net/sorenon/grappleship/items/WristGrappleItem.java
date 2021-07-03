package net.sorenon.grappleship.items;

import net.sorenon.grappleship.GrappleShipMod;
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
            double distance = 10;
            Vec3d start = user.getCameraPosVec(1.0f);
            Vec3d look = user.getRotationVec(1.0f);
            Vec3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
            HitResult res = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));

            if (res.getType() != HitResult.Type.MISS) {
                var buf = PacketByteBufs.create();
                buf.writeDouble(res.getPos().x);
                buf.writeDouble(res.getPos().y);
                buf.writeDouble(res.getPos().z);
                ClientPlayNetworking.send(GrappleShipMod.C2S_START_GRAPPLE, buf);

                user.getAbilities().flying = false;

                GrappleHookMovement.start(user, res.getPos());

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
                ClientPlayNetworking.send(GrappleShipMod.C2S_END_GRAPPLE, buf);
                movement.end(user, false);
            }
        }
    }
}
