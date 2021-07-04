package net.sorenon.grappleship;

import net.fabricmc.api.ModInitializer;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.items.GrappleHookItem;
import net.sorenon.grappleship.items.WristGrappleItem;
import net.sorenon.grappleship.movement.GrappleHookMovement;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

public class GrappleShipMod implements ModInitializer {

	public static final Identifier C2S_START_GRAPPLE = new Identifier("grappleship", "c_grapple_start");
	public static final Identifier C2S_END_GRAPPLE = new Identifier("grappleship", "c_grapple_end");

	public static final Identifier S2C_START_GRAPPLE = new Identifier("grappleship", "s_grapple_start");
	public static final Identifier S2C_END_GRAPPLE = new Identifier("grappleship", "s_grapple_end");

	public static final WristGrappleItem WRIST_GRAPPLE_ITEM = new WristGrappleItem(new FabricItemSettings().maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier("grappleship", "grapple"), new GrappleHookItem(new FabricItemSettings().maxCount(1)));
		Registry.register(Registry.ITEM, new Identifier("grappleship", "grapple2"), WRIST_GRAPPLE_ITEM);

		ServerPlayNetworking.registerGlobalReceiver(C2S_START_GRAPPLE, (server, player, handler, buf, responseSender) -> {
			Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
			int entityId = buf.readInt();

			server.execute(() -> {
				PacketByteBuf buf2 = PacketByteBufs.create();
				buf2.writeDouble(pos.x);
				buf2.writeDouble(pos.y);
				buf2.writeDouble(pos.z);
				buf2.writeInt(entityId);
				buf2.writeInt(player.getId());

				for (var p : PlayerLookup.tracking(player)) {
					if (p != player) {
						ServerPlayNetworking.send(p, S2C_START_GRAPPLE, buf2);
					}
				}

				GrappleHookMovement.start(player, pos, player.world.getEntityById(entityId));
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
