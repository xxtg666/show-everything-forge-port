package dev.minerslab.showeverything.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.Optional;

public final class Raycasts {
    private Raycasts() {
    }

    public static RayTraceResult blocks(ServerPlayerEntity player, double distance, boolean fluids) {
        Vector3d eyes = player.getEyePosition(1.0F);
        Vector3d end = eyes.add(player.getViewVector(1.0F).scale(distance));
        return player.level.clip(new RayTraceContext(eyes, end,
                RayTraceContext.BlockMode.COLLIDER,
                fluids ? RayTraceContext.FluidMode.ANY : RayTraceContext.FluidMode.NONE,
                player));
    }

    public static RayTraceResult entity(ServerPlayerEntity player, double distance) {
        Vector3d eyes = player.getEyePosition(1.0F);
        Vector3d look = player.getViewVector(1.0F);
        Vector3d end = eyes.add(look.scale(distance));
        RayTraceResult blockHit = player.level.clip(new RayTraceContext(eyes, end,
                RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, player));
        if (blockHit.getType() != RayTraceResult.Type.MISS) {
            end = blockHit.getLocation();
            distance = Math.sqrt(eyes.distanceToSqr(end));
        }

        Entity closest = null;
        Vector3d closestHit = null;
        double closestDistance = distance * distance;
        AxisAlignedBB search = player.getBoundingBox().expandTowards(look.scale(distance)).inflate(1.0D);
        List<Entity> entities = player.level.getEntities(player, search, Entity::isPickable);
        for (Entity entity : entities) {
            AxisAlignedBB box = entity.getBoundingBox().inflate(entity.getPickRadius());
            Optional<Vector3d> hit = box.clip(eyes, end);
            if (!hit.isPresent()) {
                continue;
            }
            double hitDistance = eyes.distanceToSqr(hit.get());
            if (hitDistance < closestDistance) {
                closest = entity;
                closestHit = hit.get();
                closestDistance = hitDistance;
            }
        }
        return closest == null ? null : new EntityRayTraceResult(closest, closestHit);
    }
}
