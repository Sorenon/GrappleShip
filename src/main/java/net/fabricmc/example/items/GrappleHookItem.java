package net.fabricmc.example.items;

import net.fabricmc.example.accessors.LivingEntityExt;
import net.fabricmc.example.movement.AirStrafeMovement;
import net.fabricmc.example.movement.GrappleHookMovement;
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

            if (ext.getMovement() instanceof GrappleHookMovement) {
                ext.setMovement(new AirStrafeMovement(0.05));
                user.playSound(SoundEvents.ITEM_SPYGLASS_STOP_USING, 1.0f, 1.0f);
                return new TypedActionResult<>(ActionResult.SUCCESS, user.getStackInHand(hand));
            } else {
                double distance = 60;
                Vec3d start = user.getCameraPosVec(1.0f);
                Vec3d look = user.getRotationVec(1.0f);
                Vec3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
                HitResult res = world.raycast(new RaycastContext(start, end, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, user));

                if (res.getType() != HitResult.Type.MISS) {
                    user.playSound(SoundEvents.ITEM_SPYGLASS_USE, 1.0f, 1.0f);
                    ((LivingEntityExt)user).setMovement(new GrappleHookMovement(res.getPos()));

//                    user.setVelocity(user.getVelocity().multiply(1,0.6,1));
                    user.getAbilities().flying = false;
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
