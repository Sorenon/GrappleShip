package net.sorenon.grappleship.worldshell;

import com.badlogic.gdx.math.Vector3;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;

public class BoxAirShip extends Box {
    private final GhastAirShip airShip;

    public BoxAirShip(double x1, double y1, double z1, double x2, double y2, double z2, GhastAirShip airShip) {
        super(x1, y1, z1, x2, y2, z2);
        this.airShip = airShip;
    }

    @Override
    public Box expand(double x, double y, double z) {
        double d = this.minX - x;
        double e = this.minY - y;
        double f = this.minZ - z;
        double g = this.maxX + x;
        double h = this.maxY + y;
        double i = this.maxZ + z;
        return new BoxAirShip(d, e, f, g, h, i, airShip);
    }

    @Override
    public boolean contains(double x, double y, double z) {
        return false; //TODO improve
    }

    @Override
    public Optional<Vec3d> raycast(Vec3d start, Vec3d end) {
//        if (!this.contains(start)) {
//            return super.raycast(start, end);
//        }

        Vec3d startLocal = airShip.toLocal(start);
        Vec3d endLocal = airShip.toLocal(end);

        double yTranslate = -3.5;
        double halfExtent = 4.5f / 2;
        Box ghastBox = new Box(-halfExtent, -halfExtent + yTranslate, -halfExtent, halfExtent, halfExtent + yTranslate, halfExtent);
        var o = ghastBox.raycast(startLocal, endLocal);
        if (o.isPresent()) {
            endLocal = o.get();
        }

        RaycastContext rayCtx = new RaycastContext(startLocal,
                endLocal, RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, airShip);
        HitResult result = airShip.getMicrocosm().raycast(rayCtx);
        if (result.getType() == HitResult.Type.MISS) {
            return o.map(airShip::toGlobal);
        }
        return Optional.of(airShip.toGlobal(result.getPos()));
    }
}
