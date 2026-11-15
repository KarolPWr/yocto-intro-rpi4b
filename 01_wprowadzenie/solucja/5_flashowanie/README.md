# Rozwiązanie: wgranie obrazu i pierwszy boot

## Zadanie

Parametry, z którymi wystartował kernel (kernel command line), są w `/proc/cmdline`:

    cat /proc/cmdline

Opis parametrów: [Linux kernel - The kernel's command-line parameters](https://docs.kernel.org/admin-guide/kernel-parameters.html),
[Raspberry Pi Documentation - Configure the kernel command line](https://www.raspberrypi.com/documentation/computers/configuration.html#cmdline).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Potrzebny jest zbudowany obraz z ćwiczenia [4_pierwszy_build](../4_pierwszy_build/README.md). Potem:

    cd ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64
    lsblk                                   # znajdź kartę SD
    sudo umount /dev/<DEVICE>*
    sudo bmaptool copy core-image-base-raspberrypi4-64.rootfs.wic.bz2 /dev/<DEVICE>
    sync
