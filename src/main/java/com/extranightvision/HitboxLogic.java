package com.extranightvision;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Decides which entities get a bigger hitbox and computes it. Never changes the real entity data. */
public final class HitboxLogic {
    private HitboxLogic() {}

    public static boolean shouldEnlarge(Entity entity) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player) {
            return false; // never touch your own hitbox
        }
        String key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        if (Config.blacklist.contains(key)) {
            return false;
        }
        if (entity instanceof Player && Friends.isFriend(entity.getName().getString())) {
            return false;
        }
        if (Config.include.isEmpty()) {
            return entity instanceof LivingEntity;
        }
        return Config.include.contains(key);
    }

    /** Scales X/Z by width and Y by height, around the center of the box. */
    public static AABB scale(AABB b, double widthFactor, double heightFactor) {
        double cx = (b.minX + b.maxX) * 0.5;
        double cy = (b.minY + b.maxY) * 0.5;
        double cz = (b.minZ + b.maxZ) * 0.5;
        double hx = (b.maxX - b.minX) * 0.5 * widthFactor;
        double hy = (b.maxY - b.minY) * 0.5 * heightFactor;
        double hz = (b.maxZ - b.minZ) * 0.5 * widthFactor;
        return new AABB(cx - hx, cy - hy, cz - hz, cx + hx, cy + hy, cz + hz);
    }
}
