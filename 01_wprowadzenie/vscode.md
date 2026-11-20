# Poradnik: VS Code do pracy z Yocto

Katalog projektu Yocto to setki tysięcy plików: `tmp/` buildu, cache, pobrane repozytoria. Jeśli VS Code zacznie
je obserwować i indeksować, zajmie procesor i pamięć - akurat wtedy, kiedy potrzebuje ich bitbake.
Dlatego wyłączamy te katalogi i konfigurujemy wtyczkę do BitBake tak, żeby uruchamiała bitbake w kontenerze.

## Wrapper: bitbake z VS Code w kontenerze

Wtyczka **Yocto Project BitBake** (`yocto-project.yocto-bitbake`) do części funkcji (przejście do definicji, wartości
zmiennych) uruchamia bitbake. U nas bitbake działa w kontenerze, więc wtyczka musi wywoływać go przez skrypt,
który uruchomi polecenie w kontenerze.

Na hoście utwórz plik `~/yocto-kurs/bitbake-crops-wrapper.sh`:

    #!/bin/bash
    # uruchamia polecenie wtyczki VS Code (bitbake) w kontenerze crops/poky

    WORKSPACE_HOST=$(pwd)
    WORKSPACE_CONTAINER="/workdir"

    # ścieżki hosta w poleceniu zamieniamy na ścieżki w kontenerze
    CMD_STR="$*"
    TRANSLATED_CMD="${CMD_STR//$WORKSPACE_HOST/$WORKSPACE_CONTAINER}"

    exec docker run --rm -i \
        -v "$WORKSPACE_HOST:$WORKSPACE_CONTAINER" \
        --workdir="$WORKSPACE_CONTAINER" \
        crops/poky:ubuntu-22.04 \
        bash -c "source bitbake-builds/rpi4/build/init-build-env && $TRANSLATED_CMD"

i nadaj mu prawo wykonania:

    chmod +x ~/yocto-kurs/bitbake-crops-wrapper.sh

## Ustawienia

Otwórz w VS Code folder `~/yocto-kurs` (File -> Open Folder) i utwórz w nim plik `.vscode/settings.json`:

    {
        "files.watcherExclude": {
            "**/build/tmp/**": true,
            "**/build/cache/**": true,
            "**/cache/downloads/**": true,
            "**/cache/sstate-cache/**": true,
            "**/.sstate-cache/**": true,
            "**/.bitbake-setup-cache/**": true,
            "**/.bitbake-setup-downloads/**": true
        },
        "search.exclude": {
            "**/build/tmp/**": true,
            "**/build/cache/**": true,
            "**/cache/downloads/**": true,
            "**/cache/sstate-cache/**": true,
            "**/.sstate-cache/**": true,
            "**/.bitbake-setup-cache/**": true,
            "**/.bitbake-setup-downloads/**": true
        },
        "python.analysis.exclude": [
            "**/build/tmp",
            "**/build/cache",
            "**/cache",
            "**/.sstate-cache",
            "**/.bitbake-setup-cache",
            "**/.bitbake-setup-downloads"
        ],
        "C_Cpp.files.exclude": {
            "**/build/tmp": true,
            "**/build/cache": true,
            "**/cache": true
        },
        "git.repositoryScanIgnoredFolders": [
            "build",
            "cache",
            ".bitbake-setup-downloads"
        ],
        "files.associations": {
            "*.conf": "bitbake",
            "*.inc": "bitbake"
        },
        "bitbake.commandWrapper": "${workspaceFolder}/bitbake-crops-wrapper.sh",
        "bitbake.pathToBitbakeFolder": "${workspaceFolder}/bitbake",
        "bitbake.pathToBuildFolder": "${workspaceFolder}/bitbake-builds/rpi4/build",
        "bitbake.pathToEnvScript": "${workspaceFolder}/bitbake-builds/rpi4/build/init-build-env",
        "python.autoComplete.extraPaths": [
            "${workspaceFolder}/bitbake-builds/rpi4/layers/bitbake/lib",
            "${workspaceFolder}/bitbake-builds/rpi4/layers/openembedded-core/meta/lib",
            "${workspaceFolder}/bitbake/lib"
        ],
        "python.analysis.extraPaths": [
            "${workspaceFolder}/bitbake-builds/rpi4/layers/bitbake/lib",
            "${workspaceFolder}/bitbake-builds/rpi4/layers/openembedded-core/meta/lib",
            "${workspaceFolder}/bitbake/lib"
        ]
    }

Co wyłączamy:
- `files.watcherExclude` - VS Code nie śledzi zmian w tych katalogach (to najbardziej obciąża w trakcie buildu),
- `search.exclude` - wyszukiwanie (`Ctrl+Shift+F`) i szybkie otwieranie plików (`Ctrl+P`) je pomija,
- `python.analysis.exclude`, `C_Cpp.files.exclude` - wtyczki Python i C/C++ ich nie indeksują,
- `git.repositoryScanIgnoredFolders` - VS Code nie szuka w nich repozytoriów git.

Katalogi nadal są widoczne w eksploratorze - logi z `tmp/work/` czy obrazy z `tmp/deploy/` otworzysz normalnie.
`build/conf/` (z `local.conf`) zostaje w indeksie.

Ustawienia wtyczki:
- `bitbake.commandWrapper` - każde polecenie bitbake wtyczka uruchamia przez nasz skrypt, czyli w kontenerze,
- `bitbake.pathToBitbakeFolder` - bitbake pobrany w ćwiczeniu 2 (`~/yocto-kurs/bitbake`),
- `bitbake.pathToBuildFolder`, `bitbake.pathToEnvScript` - katalog build i skrypt środowiska,
  tak jak `source .../init-build-env` w terminalu,
- `python.*.extraPaths` - biblioteki Pythona z bitbake i oe-core: podpowiedzi we wstawkach `python` w receptach.

`files.associations` - `*.conf` i `*.inc` otwierają się jako pliki BitBake, więc `local.conf` też jest podświetlany.

## Co daje wtyczka

- podświetlanie składni recept (`.bb`, `.bbappend`, `.bbclass`, `.inc`, `.conf`), także wstawek w Pythonie i shellu,
- podpowiedzi (`Ctrl+Space`) - słowa kluczowe, override'y, flagi zmiennych, nazwy tasków,
- przejście do definicji (`Ctrl` + klik) - na `inherit`, `require`, nazwie recepty, zmiennej,
- opis zmiennej z dokumentacji po najechaniu kursorem.

Po otwarciu folderu wtyczka skanuje projekt - uruchamia bitbake w kontenerze, chwilę to trwa. Po zapisie pliku
`.bb`, `.bbappend` albo `.inc` uruchamia dla recepty `bitbake -e`.

Uwaga: wtyczka nie wie o buildzie, który idzie w terminalu. Dwa bitbake w dwóch kontenerach naraz kończą się błędem
`BrokenPipeError` albo `bitbake-server might have died`. Nie zapisuj recept w trakcie buildu - a jeśli błąd się
pojawi, poczekaj na koniec buildu i uruchom `Ctrl+Shift+P` -> `BitBake: Rescan project`.

Źródło: [vscode-bitbake - README](https://github.com/yoctoproject/vscode-bitbake#readme) („BitBake Command wrappers”,
„Additional settings recommendations”, „Go to definition”, „Recipe scan”),
[vscode-bitbake - TROUBLESHOOTING](https://github.com/yoctoproject/vscode-bitbake/blob/main/TROUBLESHOOTING.md)
(„BrokenPipeError on BitBake commands”), [VS Code - Settings](https://code.visualstudio.com/docs/configure/settings)
