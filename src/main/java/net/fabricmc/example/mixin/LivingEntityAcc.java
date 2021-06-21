package net.fabricmc.example.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAcc {

    @Invoker()
    float callGetJumpVelocity();

    @Invoker()
    boolean callShouldSwimInFluids();
}
