# Rozwiązanie: pierwszy build

## Odpowiedzi

**Wynikowy obraz:** `tmp/deploy/images/raspberrypi4-64/` w katalogu buildu. Obraz na kartę SD to
`core-image-base-raspberrypi4-64.rootfs.wic.bz2` + plik `.wic.bmap` do wgrania przez `bmaptool`.
Poza tym m.in. `.tar.bz2` i `.ext3` z samym rootfs (lista typów: `IMAGE_FSTYPES` w meta-raspberrypi,
`conf/machine/include/rpi-base.inc`). Pliki bez daty w nazwie to linki do najnowszego buildu.

**Zadanie:**
1. Tak: `ssh-server-dropbear` oraz `allow-empty-password empty-root-password allow-root-login`.
2. `bitbake-getvar` pokazuje historię zmiennej: `ssh-server-dropbear` pochodzi z `conf/local.conf`, funkcje root-login
   z pliku fragmentu `meta/conf/fragments/yocto/root-login-with-empty-password.conf` w openembedded-core
   (oba przez `EXTRA_IMAGE_FEATURES`, które `meta/conf/bitbake.conf` dokleja do `IMAGE_FEATURES`).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env
    bitbake-config-build enable-fragment core/yocto/root-login-with-empty-password

Dopisz na końcu `conf/local.conf` zawartość pliku [local.conf.dodatki](local.conf.dodatki), potem:

    # kontener
    bitbake core-image-base
