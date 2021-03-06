package net.sorenon.grappleship.movement;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;

public class FrozenMovement extends Movement {

    private final UnfreezePredicate unfreezePredicate;

    public FrozenMovement(UnfreezePredicate unfreezePredicate) {
        this.unfreezePredicate = unfreezePredicate;
    }

    @Override
    public Movement travel(LivingEntity entity, Vec3d input, boolean jumping) {
        if (unfreezePredicate.shouldUnfreeze(this, entity, input, jumping)) {
            return null;
        }
        else {
            entity.setVelocity(Vec3d.ZERO);
            return this;
        }
    }

    @Override
    public String name() {
        return "Frozen";
    }

    @FunctionalInterface
    public interface UnfreezePredicate {
        boolean shouldUnfreeze(FrozenMovement movement, LivingEntity entity, Vec3d input, boolean jumping);
    }
}
