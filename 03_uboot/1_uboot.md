# U-Boot na Raspberry Pi

Do tej pory firmware Raspberry Pi uruchamiał kernel bezpośrednio:

    firmware RPi -> kernel (Image) + device tree

Teraz wstawimy między nie U-Boota:

    firmware RPi -> U-Boot -> skrypt boot.scr -> kernel (Image) + device tree

Firmware nadal czyta `config.txt` i przygotowuje device tree. U-Boot przejmuje ładowanie kernela z karty SD.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Włączenie U-Boota

meta-raspberrypi ma gotową obsługę U-Boota - włącza ją zmienna `RPI_USE_U_BOOT`. Dopisz na końcu fragmentu
`~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf`:

    # bootloader U-Boot między firmware RPi a kernelem
    RPI_USE_U_BOOT = "1"


**Q: Pod jaką nazwą U-Boot trafi na kartę? Dlaczego akurat pod taką?**

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs.conf: U-Boot"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1. Zanim podłączysz zasilanie, otwórz konsolę UART (`picocom`).

## Konsola U-Boota

Po starcie w konsoli UART pojawi się U-Boot i odliczanie:

    Hit any key to stop autoboot:  2

Naciśnij dowolny klawisz, zanim minie czas. Pojawi się prompt `U-Boot>`. Rozejrzyj się:

    help
    version
    printenv

## Jak U-Boot uruchamia system

    printenv bootcmd

`bootcmd` to komenda, którą U-Boot wykonuje sam po odliczaniu. Szuka na dostępnych nośnikach czegoś, co umie
uruchomić - u nas skryptu `boot.scr` na partycji boot. Zobacz pliki na tej partycji (karta SD to `mmc 0`, partycja 1):

    ls mmc 0:1

`boot.scr` to skrypt U-Boota w formie binarnej. Jego źródło jest w meta-raspberrypi - obejrzyj je na hoście:

    cat ~/yocto-kurs/bitbake-builds/rpi4/layers/meta-raspberrypi/recipes-bsp/rpi-u-boot-scr/files/boot.cmd.in

Napisy `@@...@@` recepta `rpi-u-boot-scr` zamienia na wartości dla naszej maszyny. Sprawdź jakie:

    # kontener
    bitbake-getvar -r rpi-u-boot-scr --value KERNEL_IMAGETYPE
    bitbake-getvar -r rpi-u-boot-scr --value KERNEL_BOOTCMD
    bitbake-getvar -r rpi-u-boot-scr --value BOOT_MEDIA

## Zadanie - ręczny start Linuxa

Uruchom Linuxa z konsoli U-Boota, wpisując komendy ze skryptu jedna po drugiej (z podstawionymi wartościami).
Linię z `saveenv` pomiń.

## Zmienne środowiskowe

Zatrzymaj autostart i ustaw dwie zmienne - własną i czas odliczania:

    setenv kurs "Kurs Yocto"
    setenv bootdelay 5
    printenv kurs bootdelay

Zrestartuj płytkę (`reset`) i sprawdź, czy zmienne przetrwały. Potem powtórz, tym razem zapisując środowisko:

    setenv kurs "Kurs Yocto"
    setenv bootdelay 5
    saveenv
    reset

Przywróć środowisko domyślne:

    env default -a
    saveenv
    boot

Źródła:
- [meta-raspberrypi - Boot to U-Boot](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/extra-build-config.md#boot-to-u-boot)
- [Raspberry Pi Documentation - config.txt: kernel](https://www.raspberrypi.com/documentation/computers/config_txt.html#kernel)
- [U-Boot - Environment Variables](https://docs.u-boot.org/en/latest/usage/environment.html)
- [U-Boot - Standard Boot](https://docs.u-boot.org/en/latest/develop/bootstd/overview.html)
- [U-Boot - booti command](https://docs.u-boot.org/en/latest/usage/cmd/booti.html),
  [fdt command](https://docs.u-boot.org/en/latest/usage/cmd/fdt.html)
