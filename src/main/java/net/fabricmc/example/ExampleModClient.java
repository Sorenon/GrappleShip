package net.fabricmc.example;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.example.accessors.LivingEntityExt;
import net.fabricmc.example.movement.GrappleHookMovement;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;

public class ExampleModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            Entity entity = context.camera().getFocusedEntity();

            if (entity instanceof LivingEntityExt le && le.getMovement() instanceof GrappleHookMovement movement) {
                MatrixStack matrices = context.matrixStack();

                matrices.push();

                Vec3d camPos = context.camera().getPos();
                matrices.translate(-camPos.x, -camPos.y, -camPos.z);

                int endLight = MinecraftClient.getInstance().getEntityRenderDispatcher().getLight(entity, context.tickDelta());//15728880

                float yaw = entity.getYaw(context.tickDelta());

                renderChain(
                        matrices,
                        movement.target,
                        entity.getLerpedPos(context.tickDelta()).add(0,entity.getHeight() / 2,0),
                        context.consumers(),
                        endLight,
                        endLight
                );

                matrices.pop();
            }
        });
    }

    public static void renderChain(MatrixStack matrices, Vec3d startPos, Vec3d endPos, VertexConsumerProvider consumers, int startLight, int endLight) {
        matrices.push();

        matrices.translate(startPos.x, startPos.y, startPos.z);

        VertexConsumer consumer = consumers.getBuffer(RenderLayer.getEntityCutoutNoCull(new Identifier("textures/block/chain.png")));

        Vec3d delta = endPos.subtract(startPos);
        Vec3d dir = delta.normalize();

        float width = 3f / 16;
        float x = width / 2;
        float height = (float) delta.length();

        matrices.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion(getYawFromNormal(dir) + (float) Math.PI));
        matrices.multiply(Vec3f.POSITIVE_X.getRadialQuaternion(((float) Math.PI / 2) - (float) Math.asin(MathHelper.clamp(dir.y, -0.999999999, 0.999999999))));

        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(45));

        Matrix4f modelMatrix = matrices.peek().getModel();
        Matrix3f normalMatrix = matrices.peek().getNormal();
        consumer.vertex(modelMatrix, -x, height, 0).color(255, 255, 255, 255).texture(6f / 16, height).overlay(OverlayTexture.DEFAULT_UV).light(endLight)     .normal(normalMatrix, 0, 0, -1).next();
        consumer.vertex(modelMatrix, x, height, 0).color(255, 255, 255, 255).texture(0, height).overlay(OverlayTexture.DEFAULT_UV).light(endLight)            .normal(normalMatrix, 0, 0, -1).next();
        consumer.vertex(modelMatrix, x, 0, 0).color(255, 255, 255, 255).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(startLight)                .normal(normalMatrix, 0, 0, -1).next();
        consumer.vertex(modelMatrix, -x, 0, 0).color(255, 255, 255, 255).texture(6f / 16, 0).overlay(OverlayTexture.DEFAULT_UV).light(startLight)         .normal(normalMatrix, 0, 0, -1).next();

        consumer.vertex(modelMatrix, 0, height, -x).color(255, 255, 255, 255).texture(6f / 16, height).overlay(OverlayTexture.DEFAULT_UV).light(endLight)     .normal(normalMatrix, 1, 0, 0).next();
        consumer.vertex(modelMatrix, 0, height, x).color(255, 255, 255, 255).texture(0, height).overlay(OverlayTexture.DEFAULT_UV).light(endLight)            .normal(normalMatrix, 1, 0, 0).next();
        consumer.vertex(modelMatrix, 0, 0, x).color(255, 255, 255, 255).texture(0, 0).overlay(OverlayTexture.DEFAULT_UV).light(startLight)                 .normal(normalMatrix, 1, 0, 0).next();
        consumer.vertex(modelMatrix, 0, 0, -x).color(255, 255, 255, 255).texture(6f / 16, 0).overlay(OverlayTexture.DEFAULT_UV).light(startLight)          .normal(normalMatrix, 1, 0, 0).next();

        matrices.pop();
    }

    public static float getYawFromNormal(Vec3d normal) {
        if (normal.z < 0) {
            return (float) java.lang.Math.atan(normal.x / normal.z);
        }
        if (normal.z == 0) {
            return (float) (Math.PI / 2 * -MathHelper.sign(normal.x));
        }
        if (normal.z > 0) {
            return (float) (java.lang.Math.atan(normal.x / normal.z) + Math.PI);
        }
        return 0;
    }
}
