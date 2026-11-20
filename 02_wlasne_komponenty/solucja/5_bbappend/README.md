# Rozwiązanie: bbappend - własny motd

Pliki: [meta-kurs/recipes-core/base-files/](meta-kurs/recipes-core/base-files/)

    meta-kurs/recipes-core/base-files/
    ├── base-files_%.bbappend
    └── files/
        └── poky/
            └── motd

## Odpowiedzi

**Dlaczego `motd` nadal był z Poky:** w `FILESPATH` najpierw są katalogi z podkatalogiem override'u
(`files/poky-altcfg`, `files/poky`, `files/raspberrypi4-64`, ...), a dopiero na końcu zwykłe `files/`:

    .../meta-kurs/recipes-core/base-files/files/poky
    .../meta-poky/recipes-core/base-files/files/poky        <- tu jest motd z Poky
    ...
    .../meta-kurs/recipes-core/base-files/files              <- tu był nasz motd

bitbake bierze pierwszy znaleziony plik. motd z Poky leży w `files/poky/` (`poky` to override distro), więc wygrywa
z naszym plikiem w zwykłym `files/`.

**Naprawa:** przenosimy nasz `motd` do `files/poky/motd`. Na liście nasz `files/poky` jest przed tym z `meta-poky`,
więc teraz wygrywa nasz plik.

Źródła: [Yocto Reference - FILESPATH](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESPATH),
[FILESOVERRIDES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESOVERRIDES),
[FILESEXTRAPATHS](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESEXTRAPATHS);
openembedded-core wrynose: `meta/classes-global/base.bbclass` (`FILESPATH`), `meta/classes-global/utils.bbclass`
(`base_set_filespath`); meta-yocto wrynose `meta-poky/recipes-core/base-files/`.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/5_bbappend/meta-kurs/recipes-core ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "base-files: własny motd"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
