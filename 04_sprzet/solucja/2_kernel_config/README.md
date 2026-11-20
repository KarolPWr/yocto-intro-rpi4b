# Rozwiązanie: konfiguracja kernela - fragment .cfg

Pliki: [meta-kurs/recipes-kernel/linux/](meta-kurs/recipes-kernel/linux/)

    meta-kurs/recipes-kernel/linux/
    ├── files/
    │   └── kurs.cfg
    └── linux-raspberrypi_%.bbappend

`kurs.cfg`:

    CONFIG_PRINTK_CALLER=y

Opcja jest w `Kernel hacking -> printk and dmesg options -> Show caller information on printks`.

## Odpowiedzi

**Po co fragment w warstwie:** `menuconfig` zmienia tylko `.config` w katalogu buildu kernela w `tmp/work`. To katalog
roboczy bitbake, nie część projektu:
- zmiana zniknie po `bitbake -c clean linux-raspberrypi`, przy nowym buildzie z sstate albo u kolegi, który
  zbuduje projekt od zera,
- bitbake nie wie o zmianie - jego sygnatury liczą się z recepty i plików z `SRC_URI`, nie z `.config` w `tmp/work`.

Fragment w `SRC_URI` jest wersjonowany w git razem z warstwą i zmienia sygnaturę tasków kernela, więc kernel
zbuduje się z nim zawsze i wszędzie. Fragment zawiera tylko naszą zmianę, a nie cały `.config` - przy aktualizacji
kernela (nowy `defconfig`) dalej pasuje.

**Fragmenty z meta-raspberrypi:** m.in. `default-cpu-governor.cfg`, `rpi4-nvmem.cfg` (tylko RPi 4) i `vc4graphics.cfg`
(gdy `MACHINE_FEATURES` zawiera `vc4graphics`) - dopisane do `SRC_URI` w `linux-raspberrypi.inc`.

**Na płytce:** każdy komunikat kernela ma dodatkowe pole z wątkiem (`T<pid>`) albo procesorem (`C<nr>`),
np. w konsoli UART:

    [    0.000000][    T0] Booting Linux on physical CPU 0x0000000000 [0x410fd083]

(odstępy między polami mogą się różnić w zależności od programu `dmesg`).

`/proc/config.gz` udostępnia moduł `configs` (`CONFIG_IKCONFIG=m`, `CONFIG_IKCONFIG_PROC=y` w kernelu RPi).

Źródła: [Yocto Kernel Dev Manual - Creating Configuration Fragments](https://docs.yoctoproject.org/wrynose/kernel-dev/common.html#creating-configuration-fragments);
meta-raspberrypi wrynose `recipes-kernel/linux/linux-raspberrypi.inc`; kernel 6.18 `lib/Kconfig.debug`
(`PRINTK_CALLER`), `init/Kconfig` (`IKCONFIG`), `kernel/printk/printk.c` (`print_caller`).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/04_sprzet/solucja/2_kernel_config/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "linux-raspberrypi: PRINTK_CALLER"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
