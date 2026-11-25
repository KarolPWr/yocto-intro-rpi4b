# gdb i devshell

Program na płytce kończy się `Segmentation fault`. Znajdziemy błąd w gdb na płytce, poprawimy go „na szybko”
w devshellu, a na koniec przeniesiemy poprawkę do warstwy.

Wymagania: obraz `kurs-image` (`05_obraz_distro`), czujnik BME280 (`04_sprzet/4_bme280`).

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Program z błędem

`kurs-temp` czyta temperaturę z urządzenia IIO o podanej nazwie (domyślnie `bme280`). Na hoście utwórz w warstwie:

    ~/yocto-kurs/meta-kurs/recipes-kurs/kurs-temp/
    ├── kurs-temp_1.0.bb
    └── files/
        └── kurs-temp.c

`files/kurs-temp.c`:

    #include <stdio.h>
    #include <stdlib.h>
    #include <string.h>

    #define IIO_DIR "/sys/bus/iio/devices"

    // szuka urządzenia IIO o podanej nazwie, zwraca jego numer albo -1
    static int find_device(const char *name)
    {
        char path[128];
        char buf[32];

        for (int i = 0; i < 10; i++) {
            snprintf(path, sizeof(path), IIO_DIR "/iio:device%d/name", i);
            FILE *f = fopen(path, "r");
            if (!f)
                continue;
            if (fgets(buf, sizeof(buf), f)) {
                buf[strcspn(buf, "\n")] = '\0';
                if (strcmp(buf, name) == 0) {
                    fclose(f);
                    return i;
                }
            }
            fclose(f);
        }
        return -1;
    }

    // temperatura w tysięcznych częściach stopnia Celsjusza
    static long read_temp(int dev)
    {
        char path[128];
        char buf[32];

        snprintf(path, sizeof(path), IIO_DIR "/iio:device%d/in_temp_input", dev);
        FILE *f = fopen(path, "r");
        fgets(buf, sizeof(buf), f);
        fclose(f);
        return strtol(buf, NULL, 10);
    }

    int main(int argc, char *argv[])
    {
        const char *name = argc > 1 ? argv[1] : "bme280";
        int dev = find_device(name);
        long temp = read_temp(dev);

        printf("%s: %.2f C\n", name, temp / 1000.0);
        return 0;
    }

`kurs-temp_1.0.bb`:

    SUMMARY = "Temperatura z czujnika IIO - program do debugowania, kurs Yocto"
    LICENSE = "MIT"
    LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

    SRC_URI = "file://kurs-temp.c"

    S = "${UNPACKDIR}"

    do_compile() {
        ${CC} ${CFLAGS} ${LDFLAGS} kurs-temp.c -o kurs-temp
    }

    do_install() {
        install -d ${D}${bindir}
        install -m 0755 kurs-temp ${D}${bindir}
    }

## Fragment do debugowania

Narzędzia do debugowania nie należą do obrazu produktu - włączamy je na czas pracy. Zrobimy to osobnym fragmentem
konfiguracji. Na hoście utwórz `~/yocto-kurs/meta-kurs/conf/fragments/debug.conf`:

    BB_CONF_FRAGMENT_SUMMARY = "Debugowanie na płytce (kurs Yocto)"
    BB_CONF_FRAGMENT_DESCRIPTION = "gdb, gdbserver, strace; kurs-temp z symbolami i źródłami."

    # gdb, gdbserver, strace (packagegroup-core-tools-debug)
    EXTRA_IMAGE_FEATURES += "tools-debug"

    # program do debugowania + informacje do debugowania (-dbg) i źródła (-src)
    IMAGE_INSTALL:append = " kurs-temp kurs-temp-dbg kurs-temp-src"

Włącz fragment:

    # kontener
    bitbake-config-build enable-fragment meta-kurs/debug
    cat conf/toolcfg.conf

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-temp: program do debugowania, fragment debug"

Zbuduj obraz:

    # kontener
    bitbake kurs-image

Jeśli gdb nie ma w cache, bitbake go zbuduje - to potrwa dłużej. Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1.

## Crash

Na płytce:

    kurs-temp
    kurs-temp bmp180

Pierwsze wywołanie wypisze temperaturę, drugie (czujnika o takiej nazwie nie ma) skończy się `Segmentation fault`.

## gdb

Na płytce:

    gdb --args kurs-temp bmp180

W gdb:

    run
    bt

`run` uruchamia program, gdb zatrzyma go w miejscu błędu. `bt` (backtrace) pokazuje stos wywołań - ramka `#0` to
funkcja, w której program się wysypał, niżej - kto ją wywołał. Przejdź do ramki z naszą funkcją `read_temp`
i obejrzyj kod oraz zmienne (numer ramki bez `#`, np. `frame 2`):

    frame <numer ramki>
    list
    print dev
    print f
    quit

Jeśli zamiast wartości zobaczysz `<optimized out>` - kompilator (`-O2`) nie zostawił tej zmiennej w pamięci.
Ramka `#0` jest w glibc, która nie ma w obrazie źródeł (`iofgets.c: No such file or directory`) - to normalne.

Zadanie: Namierz błąd za pomocą GDB. 

## devshell

devshell to terminal ze środowiskiem, w którym bitbake wykonuje taski recepty - z cross-compilerem i jego flagami:

    # kontener
    bitbake -c devshell kurs-temp

Terminal otworzy się w katalogu ze źródłami (`S`). W devshellu:

    # devshell
    pwd
    echo $CC
    echo $CFLAGS
    $CC $CFLAGS $LDFLAGS kurs-temp.c -o kurs-temp
    file kurs-temp

`exit` zamyka devshell.

## Zadanie - poprawka

Popraw `kurs-temp`: gdy urządzenia nie ma, program ma wypisać komunikat i zakończyć się kodem `1`, zamiast się
wysypać.
1. Popraw źródło w katalogu z devshella (na hoście zamiast `/workdir` jest `~/yocto-kurs`) i skompiluj je w devshellu.
2. Skopiuj program na płytkę przez `scp` do `/tmp` i sprawdź oba wywołania z sekcji „Crash”.
3. Przenieś poprawkę do warstwy (`files/kurs-temp.c`), zbuduj receptę i zapisz zmiany w git.

Źródła:
- [Yocto Dev Manual - Debugging with the GNU Project Debugger (GDB) on the Target](https://docs.yoctoproject.org/wrynose/dev-manual/debugging.html#debugging-with-the-gnu-project-debugger-gdb-on-the-target)
- [Yocto Dev Manual - Using a Development Shell](https://docs.yoctoproject.org/wrynose/dev-manual/development-shell.html)
- [Yocto Reference - Image Features](https://docs.yoctoproject.org/wrynose/ref-manual/features.html#image-features),
  [EXTRA_IMAGE_FEATURES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-EXTRA_IMAGE_FEATURES),
  [PACKAGE_DEBUG_SPLIT_STYLE](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-PACKAGE_DEBUG_SPLIT_STYLE)
- [GDB - Backtraces](https://sourceware.org/gdb/current/onlinedocs/gdb.html/Backtrace.html)

## gdb - ściągawka

| Komenda | Skrót | Co robi |
|---|---|---|
| `gdb --args prog arg1 arg2` | | uruchamia gdb z programem i jego argumentami |
| `run` | `r` | uruchamia program |
| `start` | | uruchamia program i zatrzymuje go na początku `main` |
| `bt` | | stos wywołań (backtrace) |
| `frame 2` | `f 2` | przejście do ramki nr 2 (numer bez `#`) |
| `up` / `down` | | ramka wyżej / niżej |
| `list` | `l` | kod wokół bieżącej linii |
| `print dev` | `p dev` | wartość zmiennej lub wyrażenia |
| `info locals` | | zmienne lokalne bieżącej ramki |
| `info args` | | argumenty bieżącej funkcji |
| `break kurs-temp.c:38` | `b kurs-temp.c:38` | breakpoint w linii (albo `b read_temp` - na funkcji) |
| `info breakpoints` | `i b` | lista breakpointów |
| `delete 1` | `d 1` | usuwa breakpoint nr 1 (`d` - wszystkie) |
| `continue` | `c` | wznawia program do następnego breakpointu |
| `next` | `n` | następna linia, bez wchodzenia do funkcji |
| `step` | `s` | następna linia, z wchodzeniem do funkcji |
| `finish` | | wykonuje funkcję do końca i wraca do wywołującej |
| `watch zmienna` | | zatrzymuje program, gdy zmienna się zmieni |
| `quit` | `q` | wyjście z gdb |

`Enter` na pustej linii powtarza ostatnią komendę (wygodne przy `n` i `s`).
