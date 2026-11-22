package dev.playerheads;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Prosty config JSON: .minecraft/config/playerheads.json */
public final class Config {
    public enum NametagMode {
        /** Serwer decyduje (zachowanie vanilla). */
        SERVER,
        /** Zawsze pokazuj nicki graczy – nadpisuje ukrywanie przez serwer. */
        SHOW,
        /** Nigdy nie pokazuj nicków graczy. */
        HIDE
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("playerheads.json");
    private static Config instance = new Config();

    /** Główka obok nicku na czacie. */
    public boolean chatHeads = true;
    /** Czy rysować warstwę "hat" (druga warstwa skina) na głowie. */
    public boolean chatHeadHat = true;
    /** Rozmiar głowy w liście TAB w pikselach GUI (8 = tyle co vanilla). */
    public int tabHeadSize = 14;
    /** Pokazuj ping w liście TAB. */
    public boolean tabShowPing = true;
    /** Tryb nicków nad głowami graczy. */
    public NametagMode nametagMode = NametagMode.SERVER;

    public static Config get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Config loaded = GSON.fromJson(Files.readString(FILE), Config.class);
                if (loaded != null) instance = loaded;
            }
        } catch (Exception e) {
            System.err.println("[playerheads] Nie udało się wczytać configu: " + e);
        }
        if (instance.nametagMode == null) instance.nametagMode = NametagMode.SERVER;
        instance.tabHeadSize = Math.max(8, Math.min(32, instance.tabHeadSize));
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[playerheads] Nie udało się zapisać configu: " + e);
        }
    }
}
