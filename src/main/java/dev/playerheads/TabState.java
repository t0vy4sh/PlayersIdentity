package dev.playerheads;

import net.minecraft.network.chat.Component;

/** Nagłówek i stopka listy graczy (wysyłane przez serwer) – przechwytywane mixinem. */
public final class TabState {
    public static volatile Component header;
    public static volatile Component footer;

    private TabState() {}
}
