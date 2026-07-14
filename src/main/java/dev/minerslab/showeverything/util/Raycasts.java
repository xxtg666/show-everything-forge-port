package dev.minerslab.showeverything.util;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public final class Raycasts {
    private Raycasts() {
    }

    public static MovingObjectPosition blocks(
            EntityPlayerMP player, double distance, boolean stopOnLiquid, boolean ignoreNoBoundingBox) {
        Vec3 eyes = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = eyes.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
        return player.worldObj.rayTraceBlocks(eyes, end, stopOnLiquid);
    }

    public static MovingObjectPosition entity(EntityPlayerMP player, double distance) {
        Vec3 eyes = Vec3.createVectorHelper(player.posX, player.posY + player.getEyeHeight(), player.posZ);
        Vec3 look = player.getLook(1.0F);
        Vec3 end = eyes.addVector(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance);
        MovingObjectPosition blockHit = player.worldObj.rayTraceBlocks(eyes, end, false);
        if (blockHit != null) {
            end = blockHit.hitVec;
            distance = Math.sqrt(eyes.squareDistanceTo(end));
        }

        Entity closest = null;
        Vec3 closestHit = null;
        double closestDistance = distance * distance;
        AxisAlignedBB search = player.boundingBox
                .addCoord(look.xCoord * distance, look.yCoord * distance, look.zCoord * distance)
                .expand(1.0D, 1.0D, 1.0D);
        List entities = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, search);
        for (Object object : entities) {
            Entity entity = (Entity) object;
            if (!entity.canBeCollidedWith()) {
                continue;
            }
            float border = entity.getCollisionBorderSize();
            AxisAlignedBB box = entity.boundingBox.expand(border, border, border);
            MovingObjectPosition hit = box.calculateIntercept(eyes, end);
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
        return closest == null ? null : new MovingObjectPosition(closest, closestHit);
    }
}
