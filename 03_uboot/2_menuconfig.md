# U-Boot: menuconfig i własny prompt

Zmienimy prompt konsoli U-Boota z `U-Boot> ` na własny. Zmianę zrobimy w `menuconfig`, a zapiszemy jako fragment
konfiguracji w naszej warstwie.

Wymagania: U-Boot (ćwiczenie `03_uboot/1_uboot`).

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## menuconfig

U-Boota buduje recepta `u-boot`. Uruchom `menuconfig`:

    # kontener
    bitbake -c menuconfig u-boot

bitbake najpierw rozpakuje i skonfiguruje źródła U-Boota - to chwila. `menuconfig` otworzy się w tym samym terminalu.

Obsługa `menuconfig`:
- `/` - wyszukiwarka, wpisz nazwę opcji (bez `CONFIG_`), Enter,
- wciśnij numer wyniku (`1`) - przeskoczysz do opcji,
- spacja - opcja zaznaczona `[*]`,
- Enter na opcji z wartością (tekst, liczba) - edycja wartości,
- `Save` (zostaw nazwę `.config`), potem `Exit` aż do wyjścia.

## Zadanie - własny prompt

W `menuconfig` zmień prompt konsoli U-Boota na własny, np. `kurs> `. Zapisz konfigurację i wyjdź z `menuconfig`.

## Fragment

    # kontener
    bitbake -c diffconfig u-boot
    cat $(bitbake-getvar -r u-boot --value WORKDIR)/fragment.cfg

Na hoście utwórz w warstwie:

    ~/yocto-kurs/meta-kurs/recipes-bsp/u-boot/
    ├── u-boot_%.bbappend
    └── files/
        └── prompt.cfg

`files/prompt.cfg` - zawartość `fragment.cfg`.

`u-boot_%.bbappend`:

    FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

    SRC_URI += "file://prompt.cfg"

Recepta U-Boota łączy pliki `.cfg` z `SRC_URI` z konfiguracją płytki (`rpi_arm64_defconfig`) w tasku `do_configure`.
meta-raspberrypi też dokłada swój fragment:

    # kontener
    bitbake-getvar -r u-boot --value SRC_URI | tr ' ' '\n' | grep cfg

## Build

    # kontener
    bitbake u-boot
    grep SYS_PROMPT $(bitbake-getvar -r u-boot --value B)/.config

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "u-boot: własny prompt"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1. Otwórz konsolę UART, zatrzymaj autostart i sprawdź prompt.

Źródła:
- [Yocto Reference - do_menuconfig](https://docs.yoctoproject.org/wrynose/ref-manual/tasks.html#do-menuconfig),
  [do_diffconfig](https://docs.yoctoproject.org/wrynose/ref-manual/tasks.html#do-diffconfig)
- [U-Boot - Environment Variables](https://docs.u-boot.org/en/latest/usage/environment.html)
- openembedded-core wrynose `meta/recipes-bsp/u-boot/u-boot-configure.inc` (`.cfg` z `SRC_URI`)
