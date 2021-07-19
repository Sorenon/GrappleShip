package net.sorenon.grappleship.worldshell;

import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.snakefangox.worldshell_fork.entity.WorldShellEntity;
import net.snakefangox.worldshell_fork.storage.ShellAwareBlock;

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

        if (parent instanceof GhastAirShip ship) {
            var seats = ship.getSeats();
            if (seats.containsKey(player.getId())) return ActionResult.PASS;

            if (!world.isClient) {
                BlockPos posLocal = parent.getBay().get().toLocal(pos);

                if (seats.containsValue(posLocal)) {
                    return ActionResult.PASS;
                }

                if (player.startRiding(ship)) {
                    var seatsNew = HashBiMap.create(seats);
                    seatsNew.put(player.getId(), posLocal);
                    ship.setSeats(seatsNew);
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS;
                }
            } else {
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    VoxelShape shape = VoxelShapes.cuboid(0,0,0, 1, 0.5, 1);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }
}
