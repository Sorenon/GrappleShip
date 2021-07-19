package net.sorenon.grappleship.worldshell.client;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.GhastEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3f;
import net.snakefangox.worldshell_fork.client.WorldShellRender;
import net.sorenon.grappleship.worldshell.GhastAirShip;

public class GhastShipRenderer extends EntityRenderer<GhastAirShip> {

    private static final Identifier TEXTURE = new Identifier("textures/entity/ghast/ghast.png");
    private static final Identifier ANGRY_TEXTURE = new Identifier("textures/entity/ghast/ghast_shooting.png");

    private final GhastEntityModel<GhastAirShip> model;

    public GhastShipRenderer(EntityRendererFactory.Context context) {
        super(context);
        this.shadowRadius = 1.5f;
        model = new GhastEntityModel<>(context.getPart(EntityModelLayers.GHAST));
    }

    @Override
    public void render(GhastAirShip entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

        matrices.push();
        matrices.translate(0, 2.5, 0);
        matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180f));
        matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(yaw));
        matrices.scale(4.5F, 4.5F, 4.5F);
        model.setAngles(null,0,0,entity.age + tickDelta,0,0);
        model.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntitySolid(getTexture(entity))), light, OverlayTexture.DEFAULT_UV, 1,1,1,1);
        matrices.pop();

        matrices.push();
        var offset = entity.getBlockOffset();
        matrices.translate(offset.x, offset.y, offset.z);
        WorldShellRender.renderMicrocosm(entity.getMicrocosm(), matrices, Vec3f.POSITIVE_Y.getDegreesQuaternion(-yaw), entity.world.getRandom(), vertexConsumers, tickDelta);

        matrices.pop();
    }

    @Override
    public Identifier getTexture(GhastAirShip ghastEntity) {
        return ghastEntity.isShooting() ? ANGRY_TEXTURE : TEXTURE;
    }
}
