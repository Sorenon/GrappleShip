package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.example.accessors.LivingEntityExt;
import net.fabricmc.example.items.GrappleHookItem;
import net.fabricmc.example.movement.GrappleHookMovement;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class ExampleMod implements ModInitializer {

	public static final Identifier C2S_START_GRAPPLE = new Identifier("modid", "c_grapple_start");
	public static final Identifier C2S_END_GRAPPLE = new Identifier("modid", "c_grapple_end");

	public static final Identifier S2C_START_GRAPPLE = new Identifier("modid", "s_grapple_start");
	public static final Identifier S2C_END_GRAPPLE = new Identifier("modid", "s_grapple_end");

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("modid", "grapple"), new GrappleHookItem(new FabricItemSettings().maxCount(1)));

		ServerPlayNetworking.registerGlobalReceiver(C2S_START_GRAPPLE, (server, player, handler, buf, responseSender) -> {
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());

			server.execute(() -> {
				PacketByteBuf buf2 = PacketByteBufs.create();
				buf2.writeDouble(pos.x);
				buf2.writeDouble(pos.y);
				buf2.writeDouble(pos.z);
				buf2.writeInt(player.getId());

				for (var p : PlayerLookup.tracking(player)) {
					if (p != player) {
						ServerPlayNetworking.send(p, S2C_START_GRAPPLE, buf2);
					}
				}

				GrappleHookMovement.start(player, pos);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(C2S_END_GRAPPLE, (server, player, handler, buf, responseSender) -> {
			boolean jump = buf.readBoolean();

			server.execute(() -> {
				PacketByteBuf buf2 = PacketByteBufs.create();
				buf2.writeBoolean(jump);
				buf2.writeInt(player.getId());

				for (var p : PlayerLookup.tracking(player)) {
					if (p != player) {
						ServerPlayNetworking.send(p, S2C_END_GRAPPLE, buf2);
					}
				}

				LivingEntityExt ext = (LivingEntityExt) player;
				if (ext.getMovement() instanceof GrappleHookMovement movement) {
					movement.end(player, jump);
				}
			});
		});
	}
}
