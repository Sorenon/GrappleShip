package net.sorenon.grappleship.worldshell;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.storage.ShellAwareBlock;

public class SeatBlock extends Block implements ShellAwareBlock {
    public SeatBlock() {
        super(Settings.of(Material.WOOD));
    }
//execute in minecraft:overworld run tp @s -36.44 81.35 -54.20 -106.84 39.30
    @Override
    public ActionResult onUseWorldshell(WorldShellEntity parent, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
//        if (parent instanceof GhastAirShip ship && !ship.seatBlocks.containsKey(player)) {
//            if (!world.isClient) {
//                BlockPos posLocal = parent.getBay().get().toLocal(pos);
//
//                if (ship.seatBlocks.containsValue(posLocal)) {
//                    return ActionResult.PASS;
//                }
//
//                ship.seatBlocks.put(player, posLocal);
//                if (player.startRiding(ship)) {
//                    return ActionResult.SUCCESS;
//                } else {
//                    ship.seatBlocks.remove(player);
//                    return ActionResult.PASS;
//                }
//            } else {
//                return ship.seatBlocks.containsValue(pos) ? ActionResult.PASS : ActionResult.SUCCESS;
//            }
//        }

        return ActionResult.PASS;
    }
}
