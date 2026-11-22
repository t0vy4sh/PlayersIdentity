package dev.playerheads.mixin;

import dev.playerheads.ChatHeads;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
    /**
     * Przechwytuje każdą wiadomość trafiającą na czat (gracze + system) i dokleja główkę przed nickiem.
     * Dopasowanie po nazwie metody obejmuje oba przeciążenia addMessage; ChatHeads chroni przed duplikatem.
     */
    @ModifyVariable(method = "addMessage", at = @At("HEAD"), argsOnly = true, require = 0)
    private Component playerheads$addHead(Component message) {
        return ChatHeads.apply(message);
    }
}
