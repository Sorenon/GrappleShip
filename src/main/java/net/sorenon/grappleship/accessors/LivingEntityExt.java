package net.sorenon.grappleship.accessors;

import net.sorenon.grappleship.movement.Movement;

public interface LivingEntityExt {
    Movement getMovement();

    void setMovement(Movement movement);

    Movement getWantedGrappleMovement();

    void setWantedGrappleMovement(Movement movement);

    int getGrappleTicks();

    void setGrappleTicks(int ticks);
}
