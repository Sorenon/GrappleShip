package net.sorenon.grappleship.worldshell;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import net.snakefangox.worldshell.math.Quaternion;
import net.sorenon.grappleship.GrappleShipMod;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class GhastAirShip extends WorldShellEntity {

    private static final TrackedDataHandler<BiMap<Integer, BlockPos>> TDHTYPE = new TrackedDataHandler<>() {
        @Override
        public void write(PacketByteBuf buf, BiMap<Integer, BlockPos> value) {
            buf.writeInt(value.size());

            for (var entry : value.entrySet()) {
                buf.writeInt(entry.getKey());
                buf.writeBlockPos(entry.getValue());
            }
        }

        @Override
        public BiMap<Integer, BlockPos> read(PacketByteBuf buf) {
            int size = buf.readInt();
            var map = HashBiMap.<Integer, BlockPos>create(size);

            for (int i = 0; i < size; i++) {
                map.put(
                        buf.readInt(),
                        buf.readBlockPos()
                );
            }

            return map;
        }

        @Override
        public BiMap<Integer, BlockPos> copy(BiMap<Integer, BlockPos> value) {
            return HashBiMap.create(value);
        }
    };

    static {
        TrackedDataHandlerRegistry.register(TDHTYPE);
    }

    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastAirShip.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> READY = DataTracker.registerData(GhastAirShip.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<BiMap<Integer, BlockPos>> SEATS = DataTracker.registerData(GhastAirShip.class, TDHTYPE);

    private int fireballStrength = 1;
    private int fireballCooldown = 40;

    private float yawVelocity = 0;

    private int shootingCooldownTimer = 0;
    public int fireballCooldownTimer = 0;

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

    public boolean fireball(PlayerEntity rider) {
        if (rider != getPrimaryPassenger()) return false;

        if (!dataTracker.get(READY)) return false;

        if (this.fireballStrength == 0) return false;

        if (world.isClient) {
            return true;
        }

        if (!this.isSilent()) {
            world.syncWorldEvent(null, WorldEvents.GHAST_SHOOTS, this.getBlockPos(), 0);
        }
        this.playSound(SoundEvents.ENTITY_GHAST_SHOOT, 1, 1);

        this.dataTracker.set(SHOOTING, true);
        this.shootingCooldownTimer = 20;
        this.dataTracker.set(READY, false);
        this.fireballCooldownTimer = this.fireballCooldown;

        Vec3d fireballDir = rider.getRotationVec(1.0f);
        FireballEntity fireballEntity = new FireballEntity(world, rider, fireballDir.x, fireballDir.y, fireballDir.z, this.getFireballStrength());

        Vec3d look = this.getRotationVec(1.0F);
//        fireballEntity.setPosition(this.getX() + look.x * 4, this.getBoundingBox().minY + 2f, this.getZ() + look.z * 4);
        fireballEntity.setPosition(this.getX(), this.getBoundingBox().minY + 2f, this.getZ());
        world.spawnEntity(fireballEntity);
        return true;
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOTING, false);
        this.dataTracker.startTracking(SEATS, HashBiMap.create());
        this.dataTracker.startTracking(READY, true);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putByte("ExplosionPower", (byte) this.fireballStrength);
        nbt.putInt("Cooldown", (byte) this.fireballCooldown);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ExplosionPower", 99)) {
            this.fireballStrength = nbt.getByte("ExplosionPower");
        }
        if (nbt.contains("Cooldown", NbtType.INT)) {
            this.fireballCooldown = nbt.getByte("Cooldown");
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

    public BiMap<Integer, BlockPos> getSeats() {
        return this.dataTracker.get(SEATS);
    }

    public void setSeats(BiMap<Integer, BlockPos> seats) {
        this.dataTracker.set(SEATS, seats);
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient) {
            if (shootingCooldownTimer > 0) {
                shootingCooldownTimer -= 1;
                if (shootingCooldownTimer == 0) {
                    dataTracker.set(SHOOTING, false);
                }
            }
            if (fireballCooldownTimer > 0) {
                fireballCooldownTimer -= 1;
                if (fireballCooldownTimer == 0) {
                    dataTracker.set(READY, true);
                    if (!this.isSilent()) {
                        world.syncWorldEvent(null, WorldEvents.GHAST_WARNS, this.getBlockPos(), 0);
                    }
                }
            }
        }

        if (this.isLogicalSideForUpdatingMovement()) {
            this.updateTrackedPosition(this.getPos());

//            this.updateVelocity();
//            if (this.world.isClient) {
//                this.updatePaddles();
//                this.world.sendPacket(new BoatPaddleStateC2SPacket(this.isPaddleMoving(0), this.isPaddleMoving(1)));
//            }

            Vec3d velocity = getVelocity();
            velocity = velocity.multiply(0.92);
            yawVelocity *= 0.92;

            this.setYaw(getYaw() + yawVelocity);

            if (getPrimaryPassenger() instanceof PlayerEntity player) {
//                Vec3d look = player.getRotationVec(1.0f);
                Vec3d look = this.getRotationVec(1.0f);
                look = look.multiply(player.forwardSpeed * 0.04);
                velocity = velocity.add(look);

                if (player.isSneaking()) {
                    velocity = velocity.add(0, -0.04, 0);
                }
                if (MinecraftClient.getInstance().options.keyJump.isPressed()) {
                    velocity = velocity.add(0, 0.04, 0);
                }

                yawVelocity -= player.sidewaysSpeed * 0.2;
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

    @Override
    public void updatePassengerPosition(Entity passenger) {
        BlockPos pos = getSeats().get(passenger.getId());
        if (pos == null) {
            super.updatePassengerPosition(passenger);
        } else {
            passenger.setPosition(toGlobal(new Vec3d(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f)));
        }

        passenger.setYaw(passenger.getYaw() + this.yawVelocity);
        passenger.prevYaw += this.yawVelocity;
        passenger.setHeadYaw(passenger.getHeadYaw() + this.yawVelocity);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        var seats = getSeats();
        seats.remove(passenger.getId());
        setSeats(seats);
    }
}
