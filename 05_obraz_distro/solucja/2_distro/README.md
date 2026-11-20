# Rozwiązanie: własne distro

Pliki po ćwiczeniu:

    meta-kurs/
    ├── conf/distro/kurs.conf
    └── conf/fragments/kurs.conf      (bez LICENSE_FLAGS_ACCEPTED)
    kurs.conf.json                    (oe-fragments: "distro/kurs")

- Distro: [meta-kurs/conf/distro/kurs.conf](meta-kurs/conf/distro/kurs.conf)
- Konfiguracja bitbake-setup: [kurs.conf.json](kurs.conf.json)

## Co gdzie jest po porządkach

| Miejsce | Co | U nas |
|---|---|---|
| distro `conf/distro/kurs.conf` | polityka całego systemu - wszystkie obrazy, wszystkie recepty | init (systemd z poky-altcfg), `DISTRO_FEATURES`, licencje, nazwa |
| obraz `kurs-image.bb` | zawartość jednego obrazu | `packagegroup-kurs`, SSH, nazwa hosta |
| fragment `meta-kurs/kurs` | ustawienia płytki | UART, U-Boot, overlaye, I2C |
| `kurs.conf.json` | co składa się na build | warstwy, rewizje, maszyna, distro, fragmenty |
| `local.conf` | szybkie eksperymenty, nie trzymamy tu nic na stałe | pusty |
| `site.conf` | ustawienia komputera | ścieżki do cache (`DL_DIR`, `SSTATE_DIR`) |

## Odpowiedzi

**`DISTROOVERRIDES = "kurs"`:**

    $ bitbake-getvar --value INIT_MANAGER
    sysvinit

`poky-altcfg.conf` nie ustawia systemd wprost, tylko przez override:

    POKY_INIT_MANAGER:poky-altcfg = "systemd"

Bez `poky-altcfg` w `DISTROOVERRIDES` (czyli w `OVERRIDES`) ta linia nie działa i zostaje
`POKY_INIT_MANAGER = "sysvinit"` z `poky.conf`. `INIT_MANAGER` wybiera plik
`conf/distro/include/init-manager-<INIT_MANAGER>.inc`, a ten zmienia `DISTRO_FEATURES` (`sysvinit` zamiast `systemd`).
`diff` pokaże różnicę w `DISTRO_FEATURES`, a to zmienia sygnatury prawie wszystkich recept - build prawie od zera,
bez cache. Dlatego własny override dopisujemy, a nie podmieniamy:

    DISTROOVERRIDES:append = ":kurs"

(u nas niepotrzebny - nie mamy jeszcze ustawień `:kurs` w żadnej recepcie).

**Recepty zbudowane od nowa po zmianie distro:**
- `os-release` - `/etc/os-release` ma `ID = "${DISTRO}"`, `NAME = "${DISTRO_NAME}"`,
- `base-files` - do `/etc/issue` trafia `DISTRO_NAME` i `DISTRO_VERSION`,
- `packagegroup-base` - `SUMMARY:packagegroup-distro-base = "${DISTRO} extras"`,
- sam obraz.

Pozostałe recepty z `grep` (np. `buildtools-tarball`, `signing-keys`) nie są w naszym obrazie. `DISTRO_VERSION`
i `MAINTAINER` zostawiliśmy z `poky.conf` - `MAINTAINER` trafia do metadanych każdej paczki (`.ipk`), więc jego zmiana
przepakowałaby wszystkie paczki.

Źródła: meta-yocto wrynose `meta-poky/conf/distro/poky-altcfg.conf`, `poky.conf`; openembedded-core wrynose
`meta/conf/bitbake.conf` (`DISTROOVERRIDES`, `OVERRIDES`, `OE_FRAGMENTS_BUILTIN = "machine:MACHINE distro:DISTRO"`),
`meta/conf/distro/defaultsetup.conf`, `meta/conf/distro/include/init-manager-systemd.inc`,
`meta/recipes-core/os-release/os-release.bb`, `meta/recipes-core/base-files/base-files_3.0.14.bb`,
`meta/recipes-core/packagegroups/packagegroup-base.bb`, `meta/classes-global/package_ipk.bbclass` (`MAINTAINER`).

## Zadanie - gdzie widać nazwę distro

1. Powitanie systemd na UART przy starcie: `Welcome to Kurs Yocto 6.0.4 (wrynose)!` (`PRETTY_NAME` z `/etc/os-release`).
2. Baner przed logowaniem na UART: `Kurs Yocto 6.0.4 <nazwa hosta> ttyS0` - plik `/etc/issue` (`\n` - nazwa hosta,
   `\l` - terminal; dopisuje je `do_install_basefilesissue` w `base-files`, ćwiczenie 5 z dnia 2).
3. `cat /etc/os-release` - `ID=kurs`, `NAME="Kurs Yocto"`, `PRETTY_NAME="Kurs Yocto 6.0.4 (wrynose)"`.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/05_obraz_distro/solucja/2_distro/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cp ~/yocto-kurs/yocto-intro-rpi4b/05_obraz_distro/solucja/2_distro/kurs.conf.json ~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "distro kurs na bazie poky-altcfg"

W kontenerze:

    # kontener
    bitbake-setup update
    bitbake kurs-image

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1, plik `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.
