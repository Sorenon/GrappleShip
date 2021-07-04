package net.sorenon.grappleship.mixin;

import net.minecraft.util.Hand;
import net.sorenon.grappleship.GrappleFeatureRenderer;
import net.sorenon.grappleship.accessors.LivingEntityExt;
import net.sorenon.grappleship.movement.GrappleHookMovement;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> implements FeatureRendererContext<T, M> {

    @Inject(method = "render", at = @At("HEAD"))
    void render(T entity, float f, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci){
        LivingEntityExt ext = (LivingEntityExt) entity;

        if (ext.getMovement() instanceof GrappleHookMovement movement) {
            matrices.push();

            Vec3d handOffset;
            if (entity.getActiveHand() == Hand.MAIN_HAND) {
                handOffset = new Vec3d(0, 0, 0.3).rotateY((float) Math.toRadians(-entity.getYaw(tickDelta) - 90));
            } else {
                handOffset = new Vec3d(0, 0, -0.3).rotateY((float) Math.toRadians(-entity.getYaw(tickDelta) - 90));
            }

            GrappleFeatureRenderer.renderChain(
                    matrices,
                    movement.getTarget().subtract(entity.getLerpedPos(tickDelta)),
                    new Vec3d(0,entity.getHeight() / 2,0).add(handOffset),
                    vertexConsumers,
                    light,
                    light
            );

            matrices.pop();
        }
    }
}
