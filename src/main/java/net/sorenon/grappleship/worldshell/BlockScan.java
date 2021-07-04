package net.sorenon.grappleship.worldshell;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;

public class BlockScan implements Iterator<BlockPos> {
	private final World world;
	private final Queue<BlockPos> toCheck = new ArrayDeque<>();
	private final Set<BlockPos> added = new HashSet<>();

	public BlockScan(BlockPos start, World world) {
		this.world = world;
		toCheck.add(start);
	}

	@Override
	public boolean hasNext() {
		return !toCheck.isEmpty();
	}

	@Override
	public BlockPos next() {
		BlockPos blockPos = toCheck.remove();
		for (Direction dir : Direction.values()) {
			BlockPos offPos = blockPos.offset(dir);
			if (added.contains(offPos)) continue;
			BlockState state = world.getBlockState(offPos);
			if (state.getMaterial() == Material.AIR || state.getMaterial() == Material.SOIL || state.getMaterial() == Material.PLANT ||
				state.getHardness(world, offPos) < 0) continue;
			toCheck.add(offPos);
			added.add(offPos);
		}
		return blockPos;
	}
}
