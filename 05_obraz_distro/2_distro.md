# Własne distro

Distro to polityka całego systemu: init manager, `DISTRO_FEATURES`, format paczek, akceptowane licencje, nazwa
i wersja. Od dnia 1 używamy `poky-altcfg` - wersji referencyjnego distro Poky z systemd. Zrobimy własne distro
`kurs` na jego bazie.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Zanim zaczniesz

Distro jest wybierane fragmentem `distro/poky-altcfg` w `kurs.conf.json` (ćwiczenie 2 z dnia 1). Zobacz, co ustawia:

    # kontener
    cat /workdir/bitbake-builds/rpi4/layers/meta-yocto/meta-poky/conf/distro/poky-altcfg.conf

Zapisz obecne `DISTRO_FEATURES` - po zmianie distro porównamy:

    # kontener
    bitbake-getvar --value DISTRO_FEATURES > /tmp/distro_features.przed

`DISTRO_FEATURES` wpływa na prawie każdą receptę. Inna lista = przebudowa prawie całego systemu, bez cache.

## Plik distro

Na hoście utwórz `~/yocto-kurs/meta-kurs/conf/distro/kurs.conf`:

    # distro kursu: poky-altcfg (systemd) + nasze ustawienia
    require conf/distro/poky-altcfg.conf

    DISTRO = "kurs"
    DISTRO_NAME = "Kurs Yocto"

    # akceptacja licencji firmware WiFi/BT (linux-firmware-rpidistro)
    LICENSE_FLAGS_ACCEPTED = "synaptics-killswitch"

Nazwa pliku = nazwa distro. bitbake szuka `conf/distro/<DISTRO>.conf` we wszystkich warstwach.

## Przełączenie distro

W `~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json` zamień w `oe-fragments` `"distro/poky-altcfg"` na
`"distro/kurs"`. Zastosuj i odpowiedz `y`:

    # kontener
    bitbake-setup update
    cat conf/toolcfg.conf

Sprawdź, że distro się zmieniło, a `DISTRO_FEATURES` nie:

    # kontener
    bitbake-getvar --value DISTRO
    bitbake-getvar --value DISTRO_FEATURES | diff /tmp/distro_features.przed - && echo "bez zmian"

Teraz, gdy licencja jest w distro, usuń `LICENSE_FLAGS_ACCEPTED` z fragmentu
`~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf` - akceptowane licencje to polityka, a nie płytka.

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "distro kurs na bazie poky-altcfg"

## Budowanie

    # kontener
    bitbake kurs-image

## Na płytce

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1. 

**UWAGA!** Plik ma nową nazwę: `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.

Sprawdź, czy wszystko z poprzednich dni działa jak wcześniej:

    ls /usr/share/kurs/
    cat /usr/share/kurs/*
    systemctl status bme280-log
    lsmod | grep kurs
    cat /sys/class/leds/kurs/trigger

Źródła:
- [Yocto Dev Manual - Creating Your Own Distribution](https://docs.yoctoproject.org/wrynose/dev-manual/custom-distribution.html)
- [Yocto Reference - DISTRO](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTRO),
  [DISTRO_NAME](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTRO_NAME),
  [DISTRO_FEATURES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTRO_FEATURES)
- [Yocto Dev Manual - Creating New Configuration Fragments](https://docs.yoctoproject.org/wrynose/dev-manual/creating-fragments.html)
  (fragmenty wbudowane `machine/` i `distro/`)
- meta-yocto wrynose `meta-poky/conf/distro/poky-altcfg.conf`, `poky.conf`
