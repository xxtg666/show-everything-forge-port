package dev.minerslab.showeverything.util;

import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Raycasts {
    private static final double DISTANCE = 15.0D;

    private Raycasts() {
    }

    public static HitResult block(ServerPlayer player, boolean fluids) {
        Vec3 eyes = player.getEyePosition(1.0F);
        Vec3 end = eyes.add(player.getViewVector(1.0F).scale(DISTANCE));
        ClipContext.Fluid fluidMode = fluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        return player.serverLevel().clip(new ClipContext(eyes, end, ClipContext.Block.OUTLINE, fluidMode, player));
    }

    public static EntityHitResult entity(ServerPlayer player) {
        Vec3 eyes = player.getEyePosition(1.0F);
        Vec3 direction = player.getViewVector(1.0F);
        Vec3 end = eyes.add(direction.scale(DISTANCE));
        HitResult blockHit = player.serverLevel().clip(new ClipContext(
                eyes, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double maxDistance = DISTANCE * DISTANCE;
        if (blockHit.getType() != HitResult.Type.MISS) {
            end = blockHit.getLocation();
            maxDistance = eyes.distanceToSqr(end);
        }

        Entity closest = null;
        Vec3 closestHit = null;
        AABB search = player.getBoundingBox().expandTowards(direction.scale(DISTANCE)).inflate(1.0D);
        for (Entity candidate : player.serverLevel().getEntities(player, search,
                entity -> entity.isPickable() && !entity.isSpectator())) {
            Optional<Vec3> hit = candidate.getBoundingBox().inflate(candidate.getPickRadius()).clip(eyes, end);
            if (hit.isPresent()) {
                double distance = eyes.distanceToSqr(hit.get());
                if (distance < maxDistance) {
                    closest = candidate;
                    closestHit = hit.get();
                    maxDistance = distance;
                }
            }
        }
        return closest == null ? null : new EntityHitResult(closest, closestHit);
    }
}
