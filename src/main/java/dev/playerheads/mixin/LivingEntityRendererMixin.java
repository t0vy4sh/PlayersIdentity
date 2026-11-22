package dev.playerheads.mixin;

import dev.playerheads.NametagControl;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** LivingEntityRenderer nadpisuje shouldShowName (logika teamów z serwera) – tu ją obchodzimy. */
@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true, require = 0)
    private void playerheads$nametag(LivingEntity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        Boolean forced = NametagControl.override(entity);
        if (forced != null) cir.setReturnValue(forced);
    }
}
