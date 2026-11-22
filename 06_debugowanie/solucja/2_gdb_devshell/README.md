# Rozwiązanie: gdb i devshell

Pliki warstwy po ćwiczeniu (z poprawką z Zadania):

    meta-kurs/
    ├── conf/fragments/debug.conf
    └── recipes-kurs/kurs-temp/
        ├── files/
        │   └── kurs-temp.c
        └── kurs-temp_1.0.bb

- Fragment: [meta-kurs/conf/fragments/debug.conf](meta-kurs/conf/fragments/debug.conf)
- Recepta: [meta-kurs/recipes-kurs/kurs-temp/kurs-temp_1.0.bb](meta-kurs/recipes-kurs/kurs-temp/kurs-temp_1.0.bb)
- Program po poprawce: [meta-kurs/recipes-kurs/kurs-temp/files/kurs-temp.c](meta-kurs/recipes-kurs/kurs-temp/files/kurs-temp.c)

**Dlaczego fragment, a nie `kurs-image`:** gdb i paczki `-dbg`/`-src` są potrzebne tylko w czasie pracy. Fragment
włączasz i wyłączasz jedną komendą (`bitbake-config-build disable-fragment meta-kurs/debug`), a recepta obrazu
zostaje bez zmian. `EXTRA_IMAGE_FEATURES` jest przeznaczone do takich ustawień w konfiguracji buildu - `image.bbclass`
dopisuje je do `IMAGE_FEATURES` każdego obrazu.

Źródła: openembedded-core wrynose `meta/classes-recipe/image.bbclass` (`IMAGE_FEATURES += "${EXTRA_IMAGE_FEATURES}"`),
`meta/classes-recipe/core-image.bbclass` (`FEATURE_PACKAGES_tools-debug`),
`meta/recipes-core/packagegroups/packagegroup-core-tools-debug.bb`, `meta/lib/bbconfigbuild/configfragments.py`.

## gdb

Stos wywołań (adresy i szczegóły mogą się różnić):

    #0  0x0000007ff7e5a1b4 in fgets () from /usr/lib/libc.so.6
    #1  fgets (...) at /usr/include/bits/stdio2.h:...
    #2  read_temp (dev=-1) at /usr/src/debug/kurs-temp/1.0/kurs-temp.c:38
    #3  main (argc=<optimized out>, argv=<optimized out>) at /usr/src/debug/kurs-temp/1.0/kurs-temp.c:47

- `#0` - program wysypał się w bibliotece C (`fgets`), ale błąd jest u nas. Biblioteka nie ma u nas informacji do
  debugowania (brak `glibc-dbg`), więc gdb widzi tylko nazwę funkcji,
- `#1` - wersja `fgets` z nagłówka glibc (`_FORTIFY_SOURCE` w flagach Yocto), wkompilowana w nasz program; tej ramki
  może nie być,
- ramka z `read_temp` (`frame 2`): `dev = -1` (urządzenia nie znaleziono), `f = 0x0` (`fopen` się nie udał) -
  `fgets` dostał `NULL`.

Dwa błędy: brak sprawdzenia `dev < 0` w `main` i brak sprawdzenia `f` w `read_temp`. Kompilator ostrzegał o tym
drugim miejscu - w `log.do_compile` recepty jest `warning: ignoring return value of 'fgets'`.

**Skąd gdb ma symbole i kod:** przy pakowaniu (`do_package`) bitbake wycina informacje do debugowania z programu:
- `/usr/bin/kurs-temp` - program bez informacji do debugowania (paczka `kurs-temp`),
- `/usr/bin/.debug/kurs-temp` - informacje do debugowania (paczka `kurs-temp-dbg`); gdb znajduje je sam (szuka
  w podkatalogu `.debug` obok programu),
- `/usr/src/debug/kurs-temp/1.0/kurs-temp.c` - źródło (paczka `kurs-temp-src`). Ścieżka zgadza się z tą zapisaną
  w informacjach do debugowania, bo `CFLAGS` zawiera `-ffile-prefix-map=${S}=/usr/src/debug/${PN}/${PV}`.

Taki podział to domyślny `PACKAGE_DEBUG_SPLIT_STYLE = "debug-with-srcpkg"`.

**`<optimized out>`:** recepta kompiluje z `-O2 -g`. Dla dokładniejszego debugowania jednej recepty można dodać
w niej `DEBUG_BUILD = "1"` - wtedy `-Og` zamiast `-O2`.

Źródła: openembedded-core wrynose `meta/conf/bitbake.conf` (`PACKAGE_DEBUG_SPLIT_STYLE`, `TARGET_DBGSRC_DIR`,
`DEBUG_PREFIX_MAP`, `FULL_OPTIMIZATION`, `DEBUG_OPTIMIZATION`, `SELECTED_OPTIMIZATION`, `TARGET_CFLAGS`),
`meta/lib/oe/package.py` (`debug-with-srcpkg`: katalog `.debug`, paczka `${PN}-src`);
[Yocto Dev Manual - Debugging with GDB on the Target](https://docs.yoctoproject.org/wrynose/dev-manual/debugging.html#debugging-with-the-gnu-project-debugger-gdb-on-the-target).

## Odpowiedzi

**`hello` bez `${CFLAGS}`:** w `CFLAGS` są `-g` (informacje do debugowania) i `-ffile-prefix-map` (ścieżki do źródeł).
`CC` ich nie zawiera. `hello` nie ma więc informacji do debugowania - w `hello-dbg` jest tylko tablica symboli
wycięta z programu. gdb pokaże nazwę funkcji (`main`), ale bez pliku i numeru linii, a `list` nie zadziała.
`hello-src` będzie pusta - bitbake szuka plików źródłowych w informacjach do debugowania.

Źródła: openembedded-core wrynose `meta/conf/bitbake.conf` (`export CC`, `export CFLAGS = "${TARGET_CFLAGS}"`,
`DEBUG_LEVELFLAG ?= "-g"`).

## devshell

- `pwd` - `S` recepty, czyli `.../tmp/work/cortexa72-poky-linux/kurs-temp/1.0/sources`,
- `$CC` - `aarch64-poky-linux-gcc` z flagą procesora (`-mcpu=cortex-a72`), flagami zabezpieczeń
  (`-fstack-protector-strong`, `-O2 -D_FORTIFY_SOURCE=2`, ...) i `--sysroot` wskazującym `recipe-sysroot` recepty,
- `$CFLAGS` - `-O2 -g`, `-ffile-prefix-map=...`, `-pipe`,
- `file kurs-temp` - `ELF 64-bit LSB ... ARM aarch64`.

To te same zmienne, z których korzysta `do_compile` - w devshellu kompilujesz dokładnie tak jak bitbake.

Źródła: openembedded-core wrynose `meta/classes-global/devshell.bbclass` (`DEVSHELL_STARTDIR ?= "${S}"`),
`meta/conf/distro/include/security_flags.inc` (`TARGET_CC_ARCH:append = " ${SECURITY_CFLAGS}"`),
`meta/conf/machine/include/arm/armv8a/tune-cortexa72.inc`;
[Yocto Dev Manual - Using a Development Shell](https://docs.yoctoproject.org/wrynose/dev-manual/development-shell.html).

## Zadanie - poprawka

Poprawiony program: [kurs-temp.c](meta-kurs/recipes-kurs/kurs-temp/files/kurs-temp.c) - `main` sprawdza wynik
`find_device`, a `read_temp` zwraca błąd, gdy nie da się otworzyć albo przeczytać pliku.

Na płytce:

    kurs-temp
    kurs-temp bmp180
    echo $?

Drugie wywołanie: `kurs-temp: nie ma urządzenia IIO o nazwie bmp180`, kod `1`.

**Dlaczego poprawka musi trafić do warstwy:** devshell działa w katalogu roboczym w `tmp/work`. Plik w `S` bitbake
nadpisze przy następnym `do_unpack` (np. po zmianie recepty albo `bitbake -c clean`), a kolega budujący projekt
z git poprawki nie dostanie. Plik w `files/` zmienia też sygnaturę tasków - bitbake sam przebuduje receptę.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/06_debugowanie/solucja/2_gdb_devshell/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-temp: program do debugowania, fragment debug"

W kontenerze:

    # kontener
    bitbake-config-build enable-fragment meta-kurs/debug
    bitbake kurs-image

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1, plik `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.
