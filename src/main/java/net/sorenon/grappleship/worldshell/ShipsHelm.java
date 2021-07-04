package net.sorenon.grappleship.worldshell;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.snakefangox.worldshell.transfer.WorldShellConstructor;
import net.snakefangox.worldshell.world.Worldshell;
import net.sorenon.grappleship.GrappleshipMod;
import org.jetbrains.annotations.Nullable;

public class ShipsHelm extends Block {
	private static final BooleanProperty CONSTRUCTING = BooleanProperty.of("constructing");

	public ShipsHelm() {
		super(FabricBlockSettings.of(Material.WOOD));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (world instanceof Worldshell) return ActionResult.PASS;
		if (state.get(CONSTRUCTING)) return ActionResult.FAIL;
		if (!(world instanceof ServerWorld)) return ActionResult.SUCCESS;

		WorldShellConstructor<GhastAirShip> airshipConstructor = WorldShellConstructor.create((ServerWorld) world, GrappleshipMod.AIRSHIP_TYPE, pos, new BlockScan(pos, world));
		world.setBlockState(pos, state.with(CONSTRUCTING, true));
		airshipConstructor.construct();
		return ActionResult.SUCCESS;
	}

	@Nullable
	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(CONSTRUCTING, false);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(CONSTRUCTING);
	}
}
