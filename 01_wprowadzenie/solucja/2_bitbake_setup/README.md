# Rozwiązanie: bitbake-setup

## Odpowiedzi

**Hash w `rev`:** gałąź się zmienia - ktoś, kto zrobi `init` za tydzień, dostałby inne warstwy niż reszta grupy,
a gotowy sstate-cache przestałby pasować. Hash commita daje zawsze dokładnie ten sam kod.

**Zadanie:** `bitbake-layers show-machines` pokazuje maszyny ze wszystkich warstw - z openembedded-core są np. maszyny
`qemu*`. Maszyny z meta-raspberrypi zaczynają się od `raspberrypi`. W gałęzi wrynose jest ich 17
(pliki w `meta-raspberrypi/conf/machine/`):

- płytki: `raspberrypi` (+ alias `raspberrypi-cm`), `raspberrypi0`, `raspberrypi0-wifi`, `raspberrypi0-2w`,
  `raspberrypi0-2w-64`, `raspberrypi2`, `raspberrypi2-64`, `raspberrypi3`, `raspberrypi3-64`, `raspberrypi4`,
  `raspberrypi4-64`, `raspberrypi5`, `raspberrypi-cm3`, `raspberrypi-cm5-io-board`,
- „multi-board”: `raspberrypi-armv7`, `raspberrypi-armv8` - jeden obraz dla wszystkich płytek ARMv7 albo ARMv8.

Część płytek ma dwie maszyny: 32 bit i 64 bit (np. `raspberrypi4` i `raspberrypi4-64`).

Źródło: [meta-raspberrypi - Layer Contents](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/layer-contents.md)

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cd ~/yocto-kurs
    git clone https://github.com/KarolPWr/yocto-intro-rpi4b.git

W kontenerze:

    # kontener
    cd /workdir
    git clone -b 2.18 https://git.openembedded.org/bitbake
    ./bitbake/bin/bitbake-setup init --non-interactive /workdir/yocto-intro-rpi4b/kurs.conf.json
    source /workdir/bitbake-builds/rpi4/build/init-build-env
