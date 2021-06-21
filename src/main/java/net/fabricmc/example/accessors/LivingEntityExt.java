package net.fabricmc.example.accessors;

import net.fabricmc.example.movement.Movement;

public interface LivingEntityExt {
    Movement getMovement();

    void setMovement(Movement movement);
}
