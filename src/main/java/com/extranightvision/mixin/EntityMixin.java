package com.extranightvision.mixin;

import com.extranightvision.Config;
import com.extranightvision.HitboxLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // Small per-entity cache so we do not create a new box on every call.
    @Unique private AABB enb$lastBase;
    @Unique private AABB enb$lastResult;
    @Unique private int enb$lastVersion = -1;

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void enb$enlarge(CallbackInfoReturnable<AABB> cir) {
        if (!Config.enabled) {
            return; // feature OFF: vanilla box is returned untouched
        }
        Entity self = (Entity) (Object) this;
        if (self.level() == null || !self.level().isClientSide()) {
            return; // only the client-side copy is changed
        }
        AABB base = cir.getReturnValue();
        int version = Config.version;
        if (base == enb$lastBase && version == enb$lastVersion) {
            cir.setReturnValue(enb$lastResult);
            return;
        }
        AABB result = HitboxLogic.shouldEnlarge(self)
                ? HitboxLogic.scale(base, Config.width, Config.height)
                : base;
        enb$lastBase = base;
        enb$lastResult = result;
        enb$lastVersion = version;
        cir.setReturnValue(result);
    }
}
