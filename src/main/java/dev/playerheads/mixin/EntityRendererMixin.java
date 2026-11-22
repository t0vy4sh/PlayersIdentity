package dev.playerheads.mixin;

import dev.playerheads.NametagControl;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true, require = 0)
    private void playerheads$nametag(Entity entity, double distanceSq, CallbackInfoReturnable<Boolean> cir) {
        Boolean forced = NametagControl.override(entity);
        if (forced != null) cir.setReturnValue(forced);
    }
}
