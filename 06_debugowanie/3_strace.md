# strace

Nie zawsze mamy źródła programu i informacje do debugowania - np. gdy dostawca daje gotową binarkę. Wtedy wciąż
widać, o co program prosi kernel: jakie pliki otwiera, co czyta i pisze, jakie procesy uruchamia. Pokazuje to
`strace` - wypisuje wywołania systemowe (syscalle) programu, ich argumenty i wyniki.

Wymagania: obraz `kurs-image` (`05_obraz_distro`).

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Program

`streaming-app` to aplikacja do oglądania filmów z dwoma trybami:
- Freemium - tylko zwiastuny,
- Premium - cały katalog.

Na hoście utwórz w warstwie:

    ~/yocto-kurs/meta-kurs/recipes-kurs/streaming-app/
    ├── streaming-app_1.0.bb
    └── files/
        └── streaming-app.c

`files/streaming-app.c` - potraktuj go jak zamkniętą binarkę od dostawcy i nie analizuj kodu:

    #include <stdio.h>
    #include <fcntl.h>
    #include <unistd.h>

    // "zamknięty" program od dostawcy - ścieżka do licencji jest zaszyfrowana
    static const unsigned char lic[] = {
        0x75, 0x2e, 0x37, 0x2a, 0x75, 0x74, 0x2c, 0x3f, 0x34, 0x3e,
        0x35, 0x28, 0x05, 0x36, 0x33, 0x39, 0x3f, 0x34, 0x29, 0x3f,
    };

    int main(void)
    {
        char path[sizeof(lic) + 1];

        for (size_t i = 0; i < sizeof(lic); i++)
            path[i] = lic[i] ^ 0x5a;
        path[sizeof(lic)] = '\0';

        printf("streaming-app: start\n");

        int fd = open(path, O_RDONLY);
        if (fd < 0) {
            printf("brak licencji - tryb Freemium (tylko zwiastuny)\n");
            return 0;
        }
        close(fd);

        printf("licencja OK - tryb Premium (cały katalog)\n");
        return 0;
    }

`streaming-app_1.0.bb`:

    SUMMARY = "Aplikacja z licencją - program do ćwiczenia ze strace, kurs Yocto"
    LICENSE = "MIT"
    LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

    SRC_URI = "file://streaming-app.c"

    S = "${UNPACKDIR}"

    do_compile() {
        ${CC} ${CFLAGS} ${LDFLAGS} streaming-app.c -o streaming-app
    }

    do_install() {
        install -d ${D}${bindir}
        install -m 0755 streaming-app ${D}${bindir}
    }

## Fragment do debugowania

Narzędzia do debugowania nie należą do obrazu produktu - włączamy je na czas pracy osobnym fragmentem konfiguracji.
Na hoście utwórz `~/yocto-kurs/meta-kurs/conf/fragments/debug.conf`:

    BB_CONF_FRAGMENT_SUMMARY = "Debugowanie na płytce (kurs Yocto)"
    BB_CONF_FRAGMENT_DESCRIPTION = "strace; program streaming-app."

    # strace + program do debugowania
    IMAGE_INSTALL:append = " strace streaming-app"

Jeśli masz już ten fragment z ćwiczenia 2 (gdb), dopisz w nim tylko `streaming-app` do `IMAGE_INSTALL` - `strace`
jest już w `tools-debug`.

Włącz fragment:

    # kontener
    bitbake-config-build enable-fragment meta-kurs/debug
    cat conf/toolcfg.conf

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "streaming-app: program do ćwiczenia ze strace, fragment debug"

Zbuduj obraz:

    # kontener
    bitbake kurs-image

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1.

## Problem

Na płytce:

    streaming-app

Mamy wykupioną subskrypcję, a aplikacja i tak startuje w trybie Freemium.

## Pierwsze kroki ze strace

Na płytce:

    strace streaming-app

Każda linia to jeden syscall: `nazwa(argumenty) = wynik`. Przy błędzie wynik to `-1` i nazwa błędu (np. `ENOENT`).
Na początku jest dużo linii, których nasz program sam nie wywołał - to loader (`ld-linux-aarch64.so.1`), który
otwiera i mapuje do pamięci bibliotekę C. Napisy z `printf` to syscalle `write(1, ...)` - `1` to standardowe
wyjście.

`strace` pisze na standardowe wyjście błędów, więc jego linie mieszają się z wyjściem programu. Przy dłuższych
śladach zapisuj je do pliku (`-o`).

Przydatne przełączniki (można je łączyć):

| przełącznik | co robi |
|---|---|
| `-e trace=openat` | tylko wybrane syscalle (tu: otwieranie plików) |
| `-e trace=%file` | wszystkie syscalle, które dostają nazwę pliku |
| `-f` | śledzi też procesy potomne (`fork`, `exec`) |
| `-o plik` | zapis do pliku zamiast na ekran |
| `-tt` / `-T` | czas wywołania / czas trwania syscalla |
| `-y` | ścieżki zamiast samych numerów deskryptorów (`read(3</etc/hostname>, ...)`) |
| `-c` | podsumowanie: ile razy i jak długo trwał każdy syscall |
| `-p PID` | podłączenie do działającego procesu |

Porównaj, co pokazuje `-f`:

    strace -e trace=execve sh -c 'cat /etc/hostname; echo koniec'
    strace -f -e trace=execve sh -c 'cat /etc/hostname; echo koniec'

## Zadanie

1. Za pomocą `strace` znajdź, dlaczego aplikacja startuje w trybie Freemium, i napraw to na płytce - bez zmian
   w programie.
2. Zrestartuj płytkę i uruchom `streaming-app` jeszcze raz. Znowu Freemium - dlaczego? Pomoże `mount`.
3. Popraw to w warstwie: licencja ma być na miejscu po każdym starcie systemu. Wskazówka: systemd ma mechanizm do
   tworzenia plików i katalogów przy starcie (`tmpfiles.d`). Plik konfiguracyjny może zainstalować recepta
   `streaming-app` do `/etc/tmpfiles.d/`. Zbuduj obraz, wgraj go, sprawdź po restarcie i zapisz zmiany w git.

## Dodatkowo: strace na działającym procesie

Jeśli masz serwis `bme280-log` z ćwiczenia 5 (dzień 3), podłącz się do niego:

    strace -f -tt -p $(systemctl show -p MainPID --value bme280-log)

Poczekaj na kilka pomiarów i przerwij `Ctrl+C` - `strace` odłączy się, a serwis będzie działał dalej.

**Q: Jakie programy uruchamia skrypt przy każdym pomiarze?**

Źródła:
- [strace - strona podręcznika](https://man7.org/linux/man-pages/man1/strace.1.html), [strace.io](https://strace.io/)
- [Yocto Reference - FILESYSTEM_PERMS_TABLES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESYSTEM_PERMS_TABLES)
- [systemd - tmpfiles.d](https://www.freedesktop.org/software/systemd/man/latest/tmpfiles.d.html)
