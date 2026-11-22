package dev.playerheads;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.ChatFormatting;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Własna lista graczy (TAB) z większymi główkami skinów po lewej stronie nicku.
 * Zastępuje vanillowy element TAB_LIST w HUD-zie.
 */
public final class TabListRenderer {
    private static final int MAX_PLAYERS = 80;
    private static final int MAX_ROWS = 20;
    private static final int COLUMN_GAP = 5;
    private static final int PING_WIDTH = 30;
    /** Poprawka pionowa dla glifu głowy (px przed skalowaniem), jeśli głowa jest o piksel za wysoko/nisko. */
    private static final float HEAD_Y_OFFSET = 0.0f;

    private static final Comparator<PlayerInfo> ORDER = Comparator
            .comparingInt((PlayerInfo i) -> i.getGameMode() == GameType.SPECTATOR ? 1 : 0)
            .thenComparing(i -> i.getTeam() == null ? "" : i.getTeam().getName())
            .thenComparing(i -> i.getProfile().name(), String.CASE_INSENSITIVE_ORDER);

    private TabListRenderer() {}

    public static void render(GuiGraphicsExtractor g, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        ClientPacketListener connection = mc.getConnection();
        if (connection == null || mc.player == null) return;
        if (!mc.options.keyPlayerList.isDown()) return;

        List<PlayerInfo> players = new ArrayList<>(connection.getListedOnlinePlayers());
        if (mc.isLocalServer() && players.size() <= 1) return; // tak jak vanilla w singleplayerze
        if (players.isEmpty()) return;
        players.sort(ORDER);
        if (players.size() > MAX_PLAYERS) players = new ArrayList<>(players.subList(0, MAX_PLAYERS));

        Font font = mc.font;
        Config cfg = Config.get();
        int head = cfg.tabHeadSize;
        boolean ping = cfg.tabShowPing;
        int rowH = Math.max(head + 2, 11);
        int screenW = mc.getWindow().getGuiScaledWidth();
        int cx = screenW / 2;

        // nazwy
        int n = players.size();
        List<Component> names = new ArrayList<>(n);
        int maxName = 0;
        for (PlayerInfo info : players) {
            Component name = displayName(info);
            names.add(name);
            maxName = Math.max(maxName, font.width(name));
        }

        // układ kolumn (jak vanilla: max 20 wierszy w kolumnie)
        int columns = 1;
        int rows = n;
        while (rows > MAX_ROWS) {
            columns++;
            rows = (n + columns - 1) / columns;
        }

        int fixed = 2 + head + 4 + 6 + (ping ? PING_WIDTH : 0) + 2;
        int entryW = fixed + maxName;
        int avail = screenW - 40;
        if (columns * entryW + (columns - 1) * COLUMN_GAP > avail) {
            entryW = (avail - (columns - 1) * COLUMN_GAP) / columns;
        }
        int nameMax = Math.max(20, entryW - fixed);
        int totalW = columns * entryW + (columns - 1) * COLUMN_GAP;

        int y = 10;

        // nagłówek
        Component header = TabState.header;
        if (header != null && !header.getString().isEmpty()) {
            y = drawCentered(g, font, header, cx, y, Math.max(totalW, 200), avail);
            y += 1;
        }

        // tło całej listy
        int x0 = cx - totalW / 2;
        g.fill(x0 - 1, y - 1, x0 + totalW + 1, y + rows * (rowH + 1), 0x80000000);

        for (int i = 0; i < n; i++) {
            PlayerInfo info = players.get(i);
            int col = i / rows;
            int row = i % rows;
            int ex = x0 + col * (entryW + COLUMN_GAP);
            int ey = y + row * (rowH + 1);

            g.fill(ex, ey, ex + entryW, ey + rowH, 0x20FFFFFF);

            // główka (glif "object/player" przeskalowany)
            Component headComponent = HeadFactory.create(info);
            if (headComponent != null) {
                float scale = head / 8.0f;
                var pose = g.pose();
                pose.pushMatrix();
                pose.translate(ex + 2, ey + (rowH - head) / 2.0f + HEAD_Y_OFFSET);
                pose.scale(scale, scale);
                g.text(font, headComponent, 0, 0, 0xFFFFFFFF);
                pose.popMatrix();
            }

            // nick
            boolean spectator = info.getGameMode() == GameType.SPECTATOR;
            FormattedText clipped = font.substrByWidth(names.get(i), nameMax);
            FormattedCharSequence seq = Language.getInstance().getVisualOrder(clipped);
            int nx = ex + 2 + head + 4;
            int ny = ey + (rowH - 8) / 2;
            g.text(font, seq, nx, ny, spectator ? 0xFFAAAAAA : 0xFFFFFFFF);

            // ping
            if (ping) {
                int ms = info.getLatency();
                String txt = ms < 0 ? "?" : ms + "ms";
                int color = ms < 0 ? 0xFFAAAAAA : ms < 150 ? 0xFF55FF55 : ms < 300 ? 0xFFFFFF55 : 0xFFFF5555;
                g.text(font, txt, ex + entryW - 3 - font.width(txt), ny, color);
            }
        }

        y += rows * (rowH + 1) + 1;

        // stopka
        Component footer = TabState.footer;
        if (footer != null && !footer.getString().isEmpty()) {
            drawCentered(g, font, footer, cx, y + 1, Math.max(totalW, 200), avail);
        }
    }

    private static Component displayName(PlayerInfo info) {
        Component name = info.getTabListDisplayName();
        if (name == null) {
            name = PlayerTeam.formatNameForTeam(info.getTeam(), Component.literal(info.getProfile().name()));
        }
        if (info.getGameMode() == GameType.SPECTATOR) {
            name = name.copy().withStyle(ChatFormatting.ITALIC);
        }
        return name;
    }

    /** Rysuje wieloliniowy tekst wycentrowany w tle; zwraca nowe Y pod tekstem. */
    private static int drawCentered(GuiGraphicsExtractor g, Font font, Component text, int cx, int y,
                                    int wantedWidth, int maxWidth) {
        List<FormattedCharSequence> lines = font.split(text, Math.min(wantedWidth, maxWidth));
        if (lines.isEmpty()) return y;
        int w = 0;
        for (FormattedCharSequence line : lines) w = Math.max(w, font.width(line));
        g.fill(cx - w / 2 - 1, y - 1, cx + w / 2 + 1, y + lines.size() * 9, 0x80000000);
        for (FormattedCharSequence line : lines) {
            g.text(font, line, cx - font.width(line) / 2, y, 0xFFFFFFFF);
            y += 9;
        }
        return y;
    }
}
