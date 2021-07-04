package net.sorenon.grappleship;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.movement.GrappleHookMovement;
import net.sorenon.grappleship.worldshell.client.GhastShipRenderer;
import org.lwjgl.glfw.GLFW;

public class GrappleShipClient implements ClientModInitializer {

    public static KeyBinding keyBinding;

    @Override
    public void onInitializeClient() {
        keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.grapplingship.force_dismount", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_ENTER, // The keycode of the key
                "category.grapplingship" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (keyBinding.wasPressed()) {
                ClientPlayNetworking.send(GrappleShipMod.C2S_FORCE_DISMOUNT, PacketByteBufs.create());
            }
        });

        EntityRendererRegistry.INSTANCE.register(GrappleShipMod.AIRSHIP_TYPE, GhastShipRenderer::new);

        ClientPlayNetworking.registerGlobalReceiver(GrappleShipMod.S2C_START_GRAPPLE, (client, handler, buf, responseSender) -> {
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            int entityTargetId = buf.readInt();
            int id = buf.readInt();

            client.execute(() -> {
                if (handler.getWorld().getEntityById(id) instanceof LivingEntity entity) {
                    GrappleHookMovement.start(entity, pos, handler.getWorld().getEntityById(entityTargetId));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(GrappleShipMod.S2C_END_GRAPPLE, (client, handler, buf, responseSender) -> {
            boolean jump = buf.readBoolean();
            int id = buf.readInt();

            client.execute(() -> {
                if (handler.getWorld().getEntityById(id) instanceof LivingEntity entity) {
                    LivingEntityExt ext = (LivingEntityExt) entity;
                    if (ext.getMovement() instanceof GrappleHookMovement movement) {
                        movement.end(entity, jump);
                    }
                }
            });
        });


        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            Entity entity = context.camera().getFocusedEntity();

            if (entity instanceof LivingEntityExt le && le.getMovement() instanceof GrappleHookMovement movement && !context.camera().isThirdPerson()) {
                MatrixStack matrices = context.matrixStack();

                matrices.push();

                Vec3d camPos = context.camera().getPos();
                matrices.translate(-camPos.x, -camPos.y, -camPos.z);

                int endLight = MinecraftClient.getInstance().getEntityRenderDispatcher().getLight(entity, context.tickDelta());//15728880

                Vec3d endPos = entity.getLerpedPos(context.tickDelta()).add(0, entity.getHeight() / 2, 0);
                Vec3d handOffset;
                if (((LivingEntity)entity).getActiveHand() == Hand.MAIN_HAND) {
                    handOffset = new Vec3d(0, 0, 0.3).rotateY((float) Math.toRadians(-entity.getYaw(context.tickDelta()) - 90));
                } else {
                    handOffset = new Vec3d(0, 0, -0.3).rotateY((float) Math.toRadians(-entity.getYaw(context.tickDelta()) - 90));
                }

                GrappleFeatureRenderer.renderChain(
                        matrices,
                        movement.getTarget(),
                        endPos.add(handOffset),
                        context.consumers(),
                        endLight,
                        endLight
                );

                matrices.pop();
            }
        });
    }
}
