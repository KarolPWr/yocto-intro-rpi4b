# Konfiguracja kernela: fragment .cfg

Włączymy w kernelu opcję `CONFIG_PRINTK_CALLER`: przy każdym komunikacie kernela pojawi się, który wątek
(albo który procesor) go wypisał. Zmianę zrobimy w `menuconfig`, a zapiszemy jako fragment konfiguracji w naszej
warstwie.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

Na płytce zobacz, jak teraz wyglądają komunikaty kernela:

    dmesg | head -n 5

## menuconfig

Kernel RPi buduje recepta `linux-raspberrypi`. Uruchom `menuconfig`:

    # kontener
    bitbake -c menuconfig linux-raspberrypi

Za pierwszym razem bitbake rozpakuje i skonfiguruje źródła kernela - to potrwa kilka minut. `menuconfig` otworzy się
w tym samym terminalu.

W `menuconfig`:
1. `/` - wyszukiwarka, wpisz `PRINTK_CALLER`, Enter,
2. wciśnij numer wyniku (`1`) - przeskoczysz do opcji,
3. spacja - opcja zaznaczona `[*]`,
4. `Save` (zostaw nazwę `.config`), potem `Exit` aż do wyjścia.

## Fragment

`menuconfig` zmienił plik `.config` w katalogu buildu kernela. Wyciągnij diffa względem konfiguracji sprzed
`menuconfig`:

    # kontener
    bitbake -c diffconfig linux-raspberrypi

bitbake wypisze, gdzie zapisał fragment (`fragment.cfg`). Zobacz go:

    # kontener
    cat $(bitbake-getvar -r linux-raspberrypi --value WORKDIR)/fragment.cfg

**Q: Zmiana z `menuconfig` jest już w konfiguracji kernela w `tmp/work`. Po co przenosić ją do warstwy?**

Na hoście utwórz w warstwie:

    ~/yocto-kurs/meta-kurs/recipes-kernel/linux/
    ├── linux-raspberrypi_%.bbappend
    └── files/
        └── kurs.cfg

`files/kurs.cfg` - zawartość `fragment.cfg`.

`linux-raspberrypi_%.bbappend`:

    FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

    SRC_URI += "file://kurs.cfg"

Recepta kernela łączy wszystkie pliki `.cfg` z `SRC_URI` z domyślną konfiguracją (`defconfig`) w tasku
`do_kernel_configme`. Nie jesteśmy pierwsi - zobacz, jakie fragmenty dokłada już meta-raspberrypi:

    # kontener
    bitbake-getvar -r linux-raspberrypi --value SRC_URI | tr ' ' '\n' | grep cfg

## Build

    # kontener
    bitbake linux-raspberrypi

Zmiana konfiguracji oznacza kompilację całego kernela - to najdłuższy build dzisiaj. Gdy się skończy, sprawdź
konfigurację, z którą kernel został zbudowany:

    # kontener
    grep PRINTK_CALLER $(bitbake-getvar -r linux-raspberrypi --value B)/.config

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "linux-raspberrypi: PRINTK_CALLER"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1. Na płytce:

    dmesg | head -n 5

Konfigurację działającego kernela możesz też odczytać z samego kernela:

    modprobe configs
    zcat /proc/config.gz | grep PRINTK_CALLER


Źródła:
- [Yocto Kernel Dev Manual - Using menuconfig](https://docs.yoctoproject.org/wrynose/kernel-dev/common.html#using-menuconfig)
- [Yocto Kernel Dev Manual - Creating Configuration Fragments](https://docs.yoctoproject.org/wrynose/kernel-dev/common.html#creating-configuration-fragments)
- [Yocto Reference - do_menuconfig](https://docs.yoctoproject.org/wrynose/ref-manual/tasks.html#do-menuconfig),
  [do_diffconfig](https://docs.yoctoproject.org/wrynose/ref-manual/tasks.html#do-diffconfig),
  [do_kernel_configme](https://docs.yoctoproject.org/wrynose/ref-manual/tasks.html#do-kernel-configme)
