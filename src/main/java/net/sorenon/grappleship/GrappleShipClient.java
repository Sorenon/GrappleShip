package net.sorenon.grappleship;

import net.fabricmc.api.ClientModInitializer;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.movement.GrappleHookMovement;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class GrappleShipClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(GrappleShipMod.S2C_START_GRAPPLE, (client, handler, buf, responseSender) -> {
            Vec3d pos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
            int id = buf.readInt();

            client.execute(() -> {
                if (handler.getWorld().getEntityById(id) instanceof LivingEntity entity) {
                    GrappleHookMovement.start(entity, pos);
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

                GrappleFeatureRenderer.renderChain(
                        matrices,
                        movement.target,
                        entity.getLerpedPos(context.tickDelta()).add(0, entity.getHeight() / 2, 0),
                        context.consumers(),
                        endLight,
                        endLight
                );

                matrices.pop();
            }
        });
    }
}
