package net.fabricmc.example.items;

import net.fabricmc.example.ExampleMod;
import net.fabricmc.example.accessors.LivingEntityExt;
import net.fabricmc.example.movement.GrappleHookMovement;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class GrappleHookItem extends Item {
    public GrappleHookItem(Item.Settings settings) {
        super(settings);
    }

    public static Mode mode = Mode.DOT_3D;

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (world.isClient) {
            var ext = ((LivingEntityExt)user);

            if (ext.getMovement() instanceof GrappleHookMovement movement) {
                var buf = PacketByteBufs.create();
                buf.writeBoolean(false);
                ClientPlayNetworking.send(ExampleMod.C2S_END_GRAPPLE, buf);

                movement.end(user, false);

                return new TypedActionResult<>(ActionResult.SUCCESS, user.getStackInHand(hand));
            } else {
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
                    ClientPlayNetworking.send(ExampleMod.C2S_START_GRAPPLE, buf);

                    user.getAbilities().flying = false;

                    GrappleHookMovement.start(user, res.getPos());

                    return new TypedActionResult<>(ActionResult.SUCCESS, user.getStackInHand(hand));
                }
            }
        }

        return super.use(world, user, hand);
    }

    public enum Mode {
        DOT_2D, //Similar to doom eternal, allows swinging along XZ plane but y vel is constant
        DOT_3D,  //More 'realistic', closer to titan fall 2, allows swinging in all directions but is a bit harder to control because:
        //falling causes a large y vel and therefore the y axis changes very slowly
        //also delta xz is usually much longer than delta y making y change even slower
        //there will probably be a way to improve this once i have time to do proper do work on this over the 1.17 modjam
        LIN
    }
}
