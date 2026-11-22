# Player Heads (Fabric, Minecraft 26.3, tylko klient)

## Funkcje
- **Czat**: główka skina po lewej stronie nicku (wiadomości graczy oraz systemowe: join/quit, śmierci, własne formaty czatu serwera).
- **TAB**: własna lista graczy z większą główką (domyślnie 14 px) po lewej od nicku, ping, nagłówek/stopka serwera.
- **Nicki nad głowami**: klawisz **N** przełącza tryb `POKAZUJ ZAWSZE` -> `UKRYTE` -> `SERWER`.
  `POKAZUJ ZAWSZE` ignoruje ukrywanie nicków przez teamy serwera.

## Konfiguracja: `.minecraft/config/playerheads.json`
| pole | opis |
|---|---|
| `chatHeads` | główki na czacie |
| `chatHeadHat` | warstwa "hat" na głowie |
| `tabHeadSize` | rozmiar głowy w TAB (8-32) |
| `tabShowPing` | ping w TAB |
| `nametagMode` | `SERVER` / `SHOW` / `HIDE` |

## Budowanie
Wymagane: JDK 25. Najprościej: wygeneruj szablon na https://fabricmc.net/develop/template/ (MC 26.3),
podmień `src/`, `build.gradle`, `gradle.properties`, `settings.gradle` na pliki z tego projektu
(zostaw `gradlew`, `gradle/wrapper`), potem `./gradlew build` -> `build/libs/playerheads-1.0.0.jar`.

## Gotowy jar bez instalowania JDK (GitHub Actions)
1. Utwórz nowe, puste repo na GitHubie i wrzuć do niego zawartość tego folderu (razem z `.github/`).
2. Wejdź w zakładkę **Actions** -> workflow **build** (odpali się sam po pushu).
3. Po zakończeniu pobierz artefakt **playerheads-jar** -> w środku `playerheads-1.0.0.jar`
   (pomiń plik `-sources.jar`).
4. Wrzuć jar do `.minecraft/mods` razem z Fabric API 0.160.5+26.3 i Fabric Loaderem 0.19.5+ (Java 25).
