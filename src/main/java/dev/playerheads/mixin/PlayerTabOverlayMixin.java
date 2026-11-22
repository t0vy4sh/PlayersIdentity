package dev.playerheads.mixin;

import dev.playerheads.TabState;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Przechwytuje nagłówek/stopkę listy graczy, żeby nasz renderer mógł je narysować. */
@Mixin(PlayerTabOverlay.class)
public abstract class PlayerTabOverlayMixin {
    @Inject(method = "setHeader", at = @At("HEAD"), require = 0)
    private void playerheads$header(@Nullable Component header, CallbackInfo ci) {
        TabState.header = header;
    }

    @Inject(method = "setFooter", at = @At("HEAD"), require = 0)
    private void playerheads$footer(@Nullable Component footer, CallbackInfo ci) {
        TabState.footer = footer;
    }

    @Inject(method = "reset", at = @At("HEAD"), require = 0)
    private void playerheads$reset(CallbackInfo ci) {
        TabState.header = null;
        TabState.footer = null;
    }
}
