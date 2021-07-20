package net.sorenon.grappleship.worldshell;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import net.snakefangox.worldshell.entity.WorldShellEntity;
import net.snakefangox.worldshell.kevlar.PhysicsWorld;
import net.snakefangox.worldshell.math.Quaternion;
import net.sorenon.grappleship.GrappleshipMod;
import net.sorenon.grappleship.mixin.ServerPlayNetworkHandlerAcc;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class GhastAirShip extends WorldShellEntity {

    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastAirShip.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> READY = DataTracker.registerData(GhastAirShip.class, TrackedDataHandlerRegistry.BOOLEAN);
    private BiMap<Integer, BlockPos> SEATS = HashBiMap.create();

    private int fireballStrength = 1;
    private int fireballCooldown = 40;

    private float yawVelocity = 0;

    private int shootingCooldownTimer = 0;
    public int fireballCooldownTimer = 0;

    public GhastAirShip(EntityType<?> type, World world) {
        super(type, world, GrappleshipMod.AIRSHIP_SETTINGS);
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

        Vec3d wantedPos = null;
        {
            double d = 160;
            HitResult result = rider.raycast(d, 1.0f, false);
            Vec3d start = rider.getCameraPosVec(1.0f);
            double d2 = d * d;

            if (result != null) {
                d2 = result.getPos().squaredDistanceTo(start);
            }

            Vec3d look = rider.getRotationVec(1.0F);
            Vec3d end = start.add(look.x * d, look.y * d, look.z * d);
            Box box = rider.getBoundingBox().stretch(look.multiply(d)).expand(1.0D, 1.0D, 1.0D);
            EntityHitResult entityHitResult = ProjectileUtil.raycast(rider, start, end, box, (entityx) -> !entityx.isSpectator() && entityx.collides() && entityx != this, d2);
            if (entityHitResult != null) {
                if (start.squaredDistanceTo(entityHitResult.getPos()) < d2 || result == null) {
                    result = entityHitResult;
                }
            }
            if (result != null) {
                wantedPos = result.getPos();
            }
        }
        Vec3d fireballPos = new Vec3d(this.getX(), this.getBoundingBox().minY + 2f, this.getZ());

        Vec3d fireballDir;
        if (wantedPos != null) {
            fireballDir = wantedPos.subtract(fireballPos).normalize();
        } else {
            fireballDir = rider.getRotationVec(1.0f);
        }
        fireballDir.multiply(2);

        FireballEntity fireballEntity = new FireballEntity(world, rider, fireballDir.x, fireballDir.y, fireballDir.z, this.getFireballStrength());

        Vec3d look = this.getRotationVec(1.0F);
//        fireballEntity.setPosition(this.getX() + look.x * 4, this.getBoundingBox().minY + 2f, this.getZ() + look.z * 4);
        fireballEntity.setPosition(fireballPos);
        world.spawnEntity(fireballEntity);
        return true;
    }

    @Override
    public void updatePhysicsBody() {
        super.updatePhysicsBody();

        //Overwrite bounding box
        Box box = getBoundingBox();
        setBoundingBox(new BoxAirShip(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, this));
    }

    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(SHOOTING, false);
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
        return true;
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
//        return HashBiMap.create();
        return this.SEATS;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return true;
    }

    public void setSeats(BiMap<Integer, BlockPos> seats) {
        this.SEATS = seats;
        if (!this.world.isClient) {
            var buf = PacketByteBufs.create();
            buf.writeInt(this.getId());
            buf.writeInt(seats.size());

            for (var entry : seats.entrySet()) {
                buf.writeInt(entry.getKey());
                buf.writeBlockPos(entry.getValue());
            }

            PlayerLookup.tracking(this).forEach(serverPlayerEntity -> ServerPlayNetworking.send(serverPlayerEntity, GrappleshipMod.S2C_SEATS, buf));
        }
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
            for (var passenger : getPassengerList()) {
                if (passenger instanceof ServerPlayerEntity pe) {
                    ((ServerPlayNetworkHandlerAcc) pe.networkHandler).setVehicleFloatingTicks(0);
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

    //Stop ghast ship from being saved with player
    @Override
    public boolean hasPlayerRider() {
        return false;
    }

    @Override
    public void setYaw(float yaw) {
        super.setYaw(yaw);//todo last yaw
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
