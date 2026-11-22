package dev.playerheads;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class PlayerHeadsClient implements ClientModInitializer {
    public static KeyMapping nametagKey;

    @Override
    public void onInitializeClient() {
        Config.load();

        nametagKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.playerheads.nametags",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (nametagKey.consumeClick()) {
                Config cfg = Config.get();
                cfg.nametagMode = switch (cfg.nametagMode) {
                    case SERVER -> Config.NametagMode.SHOW;
                    case SHOW -> Config.NametagMode.HIDE;
                    case HIDE -> Config.NametagMode.SERVER;
                };
                Config.save();
                if (client.player != null) {
                    Component msg = switch (cfg.nametagMode) {
                        case SHOW -> Component.literal("Nicki graczy: POKAZUJ ZAWSZE (nadpisuje serwer)");
                        case HIDE -> Component.literal("Nicki graczy: UKRYTE");
                        case SERVER -> Component.literal("Nicki graczy: decyduje serwer");
                    };
                    client.player.displayClientMessage(msg, true);
                }
            }
        });

        // podmień vanillową listę graczy (TAB) na naszą, z większymi główkami
        HudElementRegistry.replaceElement(VanillaHudElements.TAB_LIST, previous -> TabListRenderer::render);
    }
}
