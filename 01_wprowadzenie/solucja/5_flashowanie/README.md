# Rozwiązanie: wgranie obrazu i pierwszy boot

## Zadanie

1. `console=serial0,115200` - konsola na UART, prędkość 115200.
   `serial0` to alias podstawowego UART-a na pinach GPIO 14/15; na RPi 4 jest to domyślnie mini UART (`/dev/ttyS0`)
   ([Raspberry Pi Documentation - Configure UARTs](https://www.raspberrypi.com/documentation/computers/configuration.html#configure-uarts)).

2. Z `ENABLE_UART = "1"` w `local.conf`. W meta-raspberrypi:
   - `recipes-bsp/bootfiles/rpi-cmdline.bb` - przy `ENABLE_UART = "1"` dodaje `console=serial0,115200` do `cmdline.txt`,
   - `recipes-bsp/bootfiles/rpi-config_git.bb` - dopisuje `enable_uart=1` do `config.txt` (włącza UART w firmware).

   Oba pliki leżą na partycji boot karty SD. Możesz je obejrzeć na płytce:

        mount | grep boot
        cat /boot/cmdline.txt /boot/config.txt

   (jeśli partycja boot nie jest zamontowana: `mount /dev/mmcblk0p1 /mnt` i zajrzyj do `/mnt`).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Potrzebny jest zbudowany obraz z ćwiczenia [4_pierwszy_build](../4_pierwszy_build/README.md). Potem:

    cd ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64
    lsblk                                   # znajdź kartę SD
    sudo umount /dev/<DEVICE>*
    sudo bmaptool copy core-image-base-raspberrypi4-64.rootfs.wic.bz2 /dev/<DEVICE>
