package net.sorenon.grappleship.worldshell;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import net.snakefangox.worldshell.math.Quaternion;
import net.sorenon.grappleship.GrappleShipMod;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class GhastAirShip extends WorldShellEntity {
    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private int fireballStrength = 1;

    public GhastAirShip(EntityType<?> type, World world) {
        super(type, world, GrappleShipMod.AIRSHIP_SETTINGS);
    }

    public boolean isShooting() {
        return this.dataTracker.get(SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.dataTracker.set(SHOOTING, shooting);
    }

    public int getFireballStrength() {
        return this.fireballStrength;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOTING, false);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("ExplosionPower", (byte) this.fireballStrength);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ExplosionPower", 99)) {
            this.fireballStrength = nbt.getByte("ExplosionPower");
        }
    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    protected void buildHullShape(PhysicsWorld physicsWorld, Set<Map.Entry<BlockPos, BlockState>> blocks) {
        super.buildHullShape(physicsWorld, blocks);

        var transform = new Matrix4();
        var shape = physicsWorld.getOrMakeBoxShape(new Vector3(4.5f / 2, 4.5f / 2, 4.5f / 2));
        var blockOffset = this.getBlockOffset();
        transform.setTranslation((float) -blockOffset.x, (float) (-2.5f - blockOffset.y), (float) -blockOffset.z);
        btHullShape.addChildShape(transform, shape);
    }

    @Nullable
    @Override
    public Entity getPrimaryPassenger() {
        return getFirstPassenger();
    }

    @Override
    public Vec3d getBlockOffset() {
        return new Vec3d(0, 1, 0);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isLogicalSideForUpdatingMovement()) {
            this.updateTrackedPosition(this.getPos());

//            this.updateVelocity();
//            if (this.world.isClient) {
//                this.updatePaddles();
//                this.world.sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
//            }

            Vec3d velocity = getVelocity();
            velocity = velocity.multiply(0.92);

            if (getPrimaryPassenger() instanceof PlayerEntity player) {
                Vec3d look = player.getRotationVec(1.0f);
                look = look.multiply(player.forwardSpeed * 0.04);
                velocity = velocity.add(look);

                float yaw = getYaw();
                yaw -= player.sidewaysSpeed * 5;
                setYaw(yaw);
            }
            this.setVelocity(velocity);
            this.velocityDirty = true;

            this.move(MovementType.SELF, this.getVelocity());
        } else {
            this.setVelocity(Vec3d.ZERO);
        }
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);
        this.setRotation(new Quaternion().fromAngles(0, Math.toRadians(-yaw), 0));
    }
//
//    public BiMap<Entity, BlockPos> seatBlocks = HashBiMap.create();
//
//    @Override
//    protected boolean canAddPassenger(Entity passenger) {
//        return seatBlocks.containsKey(passenger);
//    }
//
//    @Override
//    public void updatePassengerPosition(Entity passenger) {
//        BlockPos pos = seatBlocks.get(passenger);
//        if (pos == null) {
//            super.updatePassengerPosition(passenger);
//        } else {
//            passenger.setPosition(toGlobal(new Vec3d(pos.getX(), pos.getY(), pos.getZ())));
//        }
//    }
//
//    @Override
//    protected void removePassenger(Entity passenger) {
//        super.removePassenger(passenger);
//        seatBlocks.remove(passenger);
//    }
}
