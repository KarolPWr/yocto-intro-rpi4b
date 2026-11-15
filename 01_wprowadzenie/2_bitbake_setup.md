# Pobranie warstw: bitbake-setup

Zanim cokolwiek zbudujemy, potrzebujemy warstw (layers): oe-core, poky, BSP dla Raspberry Pi oraz bitbake.
Od wersji 6.0 (Wrynose) służy do tego narzędzie `bitbake-setup`.

`bitbake-setup` czyta plik konfiguracyjny JSON, który opisuje:
- skąd pobrać warstwy i w jakiej wersji (commit),
- które warstwy włączyć do buildu,
- które fragmenty konfiguracji włączyć (np. maszyna, distro).

Jeden plik JSON w repozytorium = cały projekt odtwarzalny u każdego, w tej samej wersji.

Źródła:
- [Yocto Quick Build - Install and use bitbake-setup](https://docs.yoctoproject.org/wrynose/brief-yoctoprojectqs/index.html#install-and-use-bitbake-setup)
- [BitBake manual - Setting up the environment with bitbake-setup](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-environment-setup.html)

## Repozytorium kursu

Na hoście:

    cd ~/yocto-kurs
    git clone https://github.com/KarolPWr/yocto-intro-rpi4b.git

Otwórz plik `~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json` w edytorze i przejrzyj go:
- `sources` - repozytoria do pobrania: `bitbake`, `openembedded-core`, `meta-yocto` (z niego bierzemy distro poky)
  i `meta-raspberrypi` (BSP),
- `bb-layers` - warstwy, które trafią do buildu,
- `oe-fragments` - fragmenty konfiguracji: maszyna `raspberrypi4-64` i distro `poky-altcfg` (poky z systemd).

**Q: Dlaczego w polu `rev` jest hash commita, a nie nazwa gałęzi (`wrynose`)?**

## bitbake-setup

`bitbake-setup` jest częścią repozytorium bitbake. Pobieramy wersję 2.18 - to wersja bitbake dla Wrynose:

    # kontener
    cd /workdir
    git clone -b 2.18 https://git.openembedded.org/bitbake

Uruchamiamy z naszym plikiem konfiguracyjnym:

    # kontener
    ./bitbake/bin/bitbake-setup init /workdir/yocto-intro-rpi4b/kurs.conf.json

`init` zada trzy pytania:
1. czy utworzyć katalog główny (top directory) i plik `site.conf` - `y`,
2. nazwa katalogu setupu - zostawiamy domyślną `rpi4` (Enter),
3. czy kontynuować - `y`.

Pobieranie warstw potrwa kilka minut.

## Co powstało

    # kontener
    ls -a /workdir/bitbake-builds
    ls /workdir/bitbake-builds/rpi4
    ls /workdir/bitbake-builds/rpi4/layers

Struktura (najważniejsze elementy):

    bitbake-builds/                  # katalog główny (top directory)
    ├── site.conf                    # ustawienia wspólne dla wszystkich setupów - zajmiemy się nim w następnym ćwiczeniu
    ├── .bitbake-setup-downloads/    # repozytoria pobrane przez bitbake-setup
    └── rpi4/                        # setup
        ├── config/                  # konfiguracja setupu z naszego JSON (repozytorium git)
        ├── layers/                  # pobrane warstwy
        │   ├── bitbake/
        │   ├── openembedded-core/
        │   ├── meta-yocto/
        │   └── meta-raspberrypi/
        └── build/                   # katalog buildu
            ├── init-build-env       # skrypt ustawiający środowisko
            ├── README
            └── conf/                # local.conf, bblayers.conf, ...

## Środowisko buildu

Żeby uruchamiać `bitbake`, trzeba najpierw ustawić środowisko. Robimy to **w każdym nowym terminalu z kontenerem**:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

Skrypt przenosi nas do katalogu `build` i dodaje narzędzia bitbake do `PATH`.

Sprawdź, jakie warstwy są w buildzie:

    # kontener
    bitbake-layers show-layers
    cat conf/bblayers.conf

## Kontrola wersji projektu

`bitbake-setup` trzyma kopię konfiguracji w repozytorium git w katalogu `config`:

    # kontener
    ls /workdir/bitbake-builds/rpi4/config

Sprawdź na wyrywki czy setup jest zgodny z plikiem JSON:

    # kontener
    bitbake-setup status

Jeśli zmienimy JSON (inna wersja warstwy, nowa warstwa), `bitbake-setup status` pokaże różnice,
a `bitbake-setup update` je zastosuje. Zrobimy to w ćwiczeniu z warstwami.

## Zadanie

Zapoznaj się z plikiem `build/README`, który wygenerował `bitbake-setup`. 

Wywołaj polecenie:

    # kontener
    bitbake-layers show-machines

Odpowiedz na pytanie: ile wersji raspberry pi jest wspieranych przez warstwę meta-raspberrypi?
