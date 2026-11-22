package dev.playerheads;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Wstawia główkę po lewej stronie pierwszego znalezionego nicku online gracza w wiadomości.
 * Działa na wiadomościach graczy i systemowych (join/quit, śmierci, własne formaty czatu).
 */
public final class ChatHeads {
    private record Segment(Style style, String text) {}

    /** Chroni przed podwójnym przetworzeniem (addMessage(Component) -> addMessage(Component, sig, tag)). */
    private static Component lastOutput;

    private ChatHeads() {}

    public static Component apply(Component message) {
        if (message == null || message == lastOutput) return message;
        if (!Config.get().chatHeads) return message;

        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return message;
        Collection<PlayerInfo> players = connection.getOnlinePlayers();
        if (players.isEmpty()) return message;

        // 1) spłaszcz komponent do segmentów (tekst + styl), zachowując kolory/kliknięcia/hovery
        List<Segment> segments = new ArrayList<>();
        StringBuilder plain = new StringBuilder();
        message.visit((style, text) -> {
            if (!text.isEmpty()) {
                segments.add(new Segment(style, text));
                plain.append(text);
            }
            return Optional.empty();
        }, Style.EMPTY);
        String s = plain.toString();
        if (s.isEmpty()) return message;

        // 2) znajdź najwcześniejszy nick (przy remisie najdłuższy)
        int bestIdx = -1;
        PlayerInfo best = null;
        for (PlayerInfo info : players) {
            String name = info.getProfile().name();
            if (name == null || name.isEmpty()) continue;
            int from = 0;
            while (true) {
                int idx = s.indexOf(name, from);
                if (idx < 0) break;
                if (isBoundary(s, idx - 1) && isBoundary(s, idx + name.length())) {
                    boolean better = best == null
                            || idx < bestIdx
                            || (idx == bestIdx && name.length() > best.getProfile().name().length());
                    if (better) {
                        bestIdx = idx;
                        best = info;
                    }
                    break;
                }
                from = idx + 1;
            }
        }
        if (best == null) return message;

        Component head = HeadFactory.create(best);
        if (head == null) return message;

        // 3) zbuduj nowy komponent z główką wstawioną przed nickiem
        MutableComponent out = Component.empty();
        int pos = 0;
        boolean inserted = false;
        for (Segment seg : segments) {
            String t = seg.text();
            int end = pos + t.length();
            if (!inserted && bestIdx >= pos && bestIdx < end) {
                int local = bestIdx - pos;
                if (local > 0) out.append(Component.literal(t.substring(0, local)).withStyle(seg.style()));
                out.append(head.copy());
                out.append(Component.literal(" "));
                out.append(Component.literal(t.substring(local)).withStyle(seg.style()));
                inserted = true;
            } else {
                out.append(Component.literal(t).withStyle(seg.style()));
            }
            pos = end;
        }
        if (!inserted) return message;

        lastOutput = out;
        return out;
    }

    private static boolean isBoundary(String s, int i) {
        if (i < 0 || i >= s.length()) return true;
        char c = s.charAt(i);
        return !(Character.isLetterOrDigit(c) || c == '_');
    }
}
