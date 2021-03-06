package net.sorenon.grappleship;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.item.group.CreativeGuiExtensions;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.snakefangox.worldshell.entity.WorldShellEntityType;
import net.snakefangox.worldshell.entity.WorldShellSettings;
import net.snakefangox.worldshell.transfer.ConflictSolver;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.items.WristGrappleItem;
import net.sorenon.grappleship.movement.GrappleHookMovement;
import net.sorenon.grappleship.worldshell.CommandStickItem;
import net.sorenon.grappleship.worldshell.GhastAirShip;
import net.sorenon.grappleship.worldshell.SeatBlock;
import net.sorenon.grappleship.worldshell.ShipsHelm;

public class GrappleshipMod implements ModInitializer {

	public static final String MODID = "grappleship";

	public static final Identifier C2S_START_GRAPPLE = new Identifier(MODID, "c_grapple_start");
	public static final Identifier C2S_END_GRAPPLE = new Identifier(MODID, "c_grapple_end");

	public static final Identifier S2C_START_GRAPPLE = new Identifier(MODID, "s_grapple_start");
	public static final Identifier S2C_END_GRAPPLE = new Identifier(MODID, "s_grapple_end");

	public static final Identifier C2S_FORCE_DISMOUNT = new Identifier(MODID, "force_dismount");
	public static final Identifier S2C_SEATS = new Identifier(MODID, "seats");

	public static final FabricItemGroupBuilder ITEM_GROUP_BASE = FabricItemGroupBuilder.create(new Identifier(MODID, "group"));
	public static final ItemGroup ITEM_GROUP = ITEM_GROUP_BASE.build();

	public static final WristGrappleItem WRIST_GRAPPLE_ITEM = new WristGrappleItem(new FabricItemSettings().maxCount(1).group(ITEM_GROUP));

	public static final ShipsHelm SHIPS_HELM = new ShipsHelm();
	public static final SeatBlock SEAT = new SeatBlock();
	public static final WorldShellSettings AIRSHIP_SETTINGS = new WorldShellSettings.Builder(true, true).setConflictSolver(ConflictSolver.HARDNESS).build();
	public static final EntityType<GhastAirShip> AIRSHIP_TYPE = new WorldShellEntityType<>(GhastAirShip::new);

	@Override
	public void onInitialize() {
//		Registry.register(Registry.ITEM, new Identifier(MODID, "grapple"), new GrappleHookItem(new FabricItemSettings().maxCount(1)));
		ITEM_GROUP_BASE.icon(() -> new ItemStack(WRIST_GRAPPLE_ITEM));

		Registry.register(Registry.ITEM, new Identifier(MODID, "wrist_grapple"), WRIST_GRAPPLE_ITEM);
		Registry.register(Registry.ITEM, new Identifier(MODID, "command_stick"), new CommandStickItem());

		Registry.register(Registry.BLOCK, new Identifier(MODID, "helm"), SHIPS_HELM);
		Registry.register(Registry.ITEM, new Identifier(MODID, "helm"), new BlockItem(SHIPS_HELM, new FabricItemSettings().group(ITEM_GROUP)));
		Registry.register(Registry.ENTITY_TYPE, new Identifier(MODID, "ghast_airship"), AIRSHIP_TYPE);

		Registry.register(Registry.BLOCK, new Identifier(MODID, "seat"), SEAT);
		Registry.register(Registry.ITEM, new Identifier(MODID, "seat"), new BlockItem(SEAT, new FabricItemSettings().group(ITEM_GROUP)));

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

		ServerPlayNetworking.registerGlobalReceiver(C2S_FORCE_DISMOUNT, (server, player, handler, buf, responseSender) -> {
			server.execute(player::stopRiding);
		});
	}
}
