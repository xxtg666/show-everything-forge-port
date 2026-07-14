package dev.minerslab.showeverything.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class Raycasts {
    private Raycasts() {
    }

    public static BlockHitResult blocks(ServerPlayer player, double distance, boolean includeFluids) {
        Vec3 eyes = player.getEyePosition(1.0F);
        Vec3 end = eyes.add(player.getViewVector(1.0F).scale(distance));
        return player.level.clip(new ClipContext(
                eyes,
                end,
                ClipContext.Block.OUTLINE,
                includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                player
        ));
    }

    public static Entity entity(ServerPlayer player, double distance) {
        Vec3 eyes = player.getEyePosition(1.0F);
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eyes.add(look.scale(distance));
        BlockHitResult blockHit = blocks(player, distance, false);
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            end = blockHit.getLocation();
            distance = eyes.distanceTo(end);
        }

        Entity closest = null;
        double closestDistance = distance * distance;
        AABB search = player.getBoundingBox().expandTowards(look.scale(distance)).inflate(1.0D);
        for (Entity candidate : player.level.getEntities(player, search, Entity::isPickable)) {
            AABB box = candidate.getBoundingBox().inflate(candidate.getPickRadius());
            Optional<Vec3> intersection = box.clip(eyes, end);
            if (box.contains(eyes)) {
                if (closestDistance >= 0.0D) {
                    closest = candidate;
                    closestDistance = 0.0D;
                }
            } else if (intersection.isPresent()) {
                double hitDistance = eyes.distanceToSqr(intersection.get());
                if (hitDistance < closestDistance) {
                    closest = candidate;
                    closestDistance = hitDistance;
                }
            }
        }
        return closest;
    }
}
