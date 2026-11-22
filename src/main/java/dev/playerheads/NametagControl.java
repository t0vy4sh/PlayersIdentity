package dev.playerheads;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Logika nadpisywania widoczności nicków nad graczami (ignoruje ustawienia teamów z serwera). */
public final class NametagControl {
    private NametagControl() {}

    /**
     * @return null = nie ingeruj (zostaw vanillę / serwer), true = pokaż, false = ukryj
     */
    public static Boolean override(Entity entity) {
        if (!(entity instanceof Player player)) return null;

        switch (Config.get().nametagMode) {
            case HIDE:
                return Boolean.FALSE;
            case SHOW: {
                Minecraft mc = Minecraft.getInstance();
                if (!Minecraft.renderNames()) return Boolean.FALSE;          // F1 – ukryty HUD
                if (player == mc.getCameraEntity()) return Boolean.FALSE;     // nie nad własną głową
                if (mc.player != null && player.isInvisibleTo(mc.player)) return Boolean.FALSE;
                return Boolean.TRUE;
            }
            default:
                return null;
        }
    }
}
