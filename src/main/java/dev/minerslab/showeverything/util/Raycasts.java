package dev.minerslab.showeverything.util;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public final class Raycasts {
    private Raycasts() {
    }

    public static RayTraceResult blocks(EntityPlayerMP player, double distance, boolean stopOnLiquid) {
        Vec3d eyes = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d end = eyes.add(look.x * distance, look.y * distance, look.z * distance);
        return player.world.rayTraceBlocks(eyes, end, stopOnLiquid, !stopOnLiquid, false);
    }

    public static RayTraceResult entity(EntityPlayerMP player, double distance) {
        Vec3d eyes = player.getPositionEyes(1.0F);
        Vec3d look = player.getLook(1.0F);
        Vec3d end = eyes.add(look.x * distance, look.y * distance, look.z * distance);
        RayTraceResult blockHit = player.world.rayTraceBlocks(eyes, end, false, true, false);
        if (blockHit != null) {
            end = blockHit.hitVec;
            distance = Math.sqrt(eyes.squareDistanceTo(end));
        }
        Entity closest = null;
        Vec3d closestHit = null;
        double closestDistance = distance * distance;

        List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(look.x * distance, look.y * distance, look.z * distance).grow(1.0D));
        for (Entity entity : entities) {
            if (!entity.canBeCollidedWith()) {
                continue;
            }
            AxisAlignedBB box = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
            RayTraceResult hit = box.calculateIntercept(eyes, end);
            if (hit == null) {
                continue;
            }
            double hitDistance = eyes.squareDistanceTo(hit.hitVec);
            if (hitDistance < closestDistance) {
                closest = entity;
                closestHit = hit.hitVec;
                closestDistance = hitDistance;
            }
        }

        return closest == null ? null : new RayTraceResult(closest, closestHit);
    }
}
