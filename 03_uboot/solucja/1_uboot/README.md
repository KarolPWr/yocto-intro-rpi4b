# Rozwiązanie: U-Boot na Raspberry Pi

Fragment po ćwiczeniu: [meta-kurs/conf/fragments/kurs.conf](meta-kurs/conf/fragments/kurs.conf)

## Odpowiedzi

**Nazwa U-Boota na karcie:** wpis `u-boot.bin;kernel8.img` - U-Boot ląduje na partycji boot jako `kernel8.img`.
To plik, który firmware RPi 4 w trybie 64-bit ładuje domyślnie jako kernel (w `config.txt` nie ma opcji `kernel`).
Firmware „myśli”, że uruchamia kernel, a uruchamia U-Boota. Prawdziwy kernel leży obok jako `Image`, a obok niego
`boot.scr`. Bez U-Boota w tym samym miejscu był wpis `Image;kernel8.img`.

    bootfiles/*
    Image
    u-boot.bin;kernel8.img
    boot.scr

Źródła: meta-raspberrypi wrynose `conf/machine/include/rpi-base.inc` (`RPI_EXTRA_IMAGE_BOOT_FILES`),
`conf/machine/raspberrypi4-64.conf` (`SDIMG_KERNELIMAGE`, `KERNEL_IMAGETYPE_UBOOT`);
[Raspberry Pi Documentation - config.txt: kernel, arm_64bit](https://www.raspberrypi.com/documentation/computers/config_txt.html#kernel).

**bootcmd:** `bootflow scan` - mechanizm Standard Boot. U-Boot przegląda nośniki i uruchamia pierwszą znalezioną
metodę startu - u nas skrypt `boot.scr` z partycji boot. W tej konfiguracji U-Boota (`rpi_arm64_defconfig`, bez
`BOOTSTD_FULL`) `bootflow` ma tylko podkomendę `scan`.

Wartości w skrypcie: `KERNEL_IMAGETYPE` = `Image`, `KERNEL_BOOTCMD` = `booti`, `BOOT_MEDIA` = `mmc`.

## Zadanie - ręczny start Linuxa

    fdt addr ${fdt_addr}
    fdt get value bootargs /chosen bootargs
    fatload mmc 0:1 ${kernel_addr_r} Image
    booti ${kernel_addr_r} - ${fdt_addr}

- `fdt addr` - wskazuje device tree, na którym działają kolejne komendy `fdt`,
- `fdt get value` - zapisuje do zmiennej `bootargs` wartość `/chosen/bootargs` z device tree,
- `fatload` - wczytuje `Image` z partycji FAT (`mmc 0:1`) do RAM pod adres `kernel_addr_r`,
- `booti` - uruchamia kernel arm64 (`Image`); `-` = bez initramfs; trzeci argument to device tree.

**Skąd `bootargs`:** firmware RPi czyta `cmdline.txt` z partycji boot (dzień 1) i wpisuje go do device tree
w węźle `/chosen` (`bootargs`). U-Boot dostaje od firmware gotowe device tree - jego adres jest w `fdt_addr` - i tylko
odczytuje z niego linię poleceń. To samo device tree, razem z overlayami z `config.txt`, dostaje potem kernel.

Źródła: meta-raspberrypi wrynose `recipes-bsp/rpi-u-boot-scr/` (`boot.cmd.in`, `rpi-u-boot-scr.bb`);
U-Boot v2026.01 `board/raspberrypi/rpi/rpi.c` (`set_fdt_addr`), `configs/rpi_arm64_defconfig`, `boot/Kconfig`
(`BOOTCOMMAND`), `cmd/bootflow.c`; [U-Boot - booti](https://docs.u-boot.org/en/latest/usage/cmd/booti.html),
[fdt](https://docs.u-boot.org/en/latest/usage/cmd/fdt.html).

## Zmienne środowiskowe

- Bez `saveenv` zmienne są tylko w RAM - po `reset` ich nie ma.
- `saveenv` zapisuje środowisko do pliku `uboot.env` na partycji boot (`CONFIG_ENV_FAT_DEVICE_AND_PART="0:1"`).
  W Linuxie to `/boot/uboot.env`. Plik powstał już przy pierwszym starcie - `boot.scr` robi `saveenv`, jeśli
  `uboot.env` nie istnieje.
- `env default -a` przywraca wszystkie zmienne do wartości domyślnych (wkompilowanych w U-Boota), `saveenv`
  zapisuje je do pliku.

Źródła: U-Boot v2026.01 `configs/rpi_arm64_defconfig`;
[U-Boot - Environment Variables](https://docs.u-boot.org/en/latest/usage/environment.html).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp ~/yocto-kurs/yocto-intro-rpi4b/03_uboot/solucja/1_uboot/meta-kurs/conf/fragments/kurs.conf ~/yocto-kurs/meta-kurs/conf/fragments/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs.conf: U-Boot"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
