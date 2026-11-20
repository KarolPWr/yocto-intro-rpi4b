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

Sprawdź distro i porównaj `DISTRO_FEATURES`:

    # kontener
    bitbake-getvar --value DISTRO
    bitbake-getvar --value DISTROOVERRIDES
    bitbake-getvar --value DISTRO_FEATURES | diff /tmp/distro_features.przed - && echo "bez zmian"

`poky-altcfg.conf` ustawia `DISTROOVERRIDES = "poky:poky-altcfg"`, a nasze distro to dziedziczy. Sprawdź, co by się
stało, gdyby distro miało tylko własny override. Dopisz na końcu `kurs.conf` (tymczasowo):

    DISTROOVERRIDES = "kurs"

i jeszcze raz:

    # kontener
    bitbake-getvar --value INIT_MANAGER
    bitbake-getvar --value DISTRO_FEATURES | diff /tmp/distro_features.przed -

**Q: Skąd ta różnica? Ile kosztowałby build z taką konfiguracją?**

Usuń dopisaną linię i sprawdź, że `DISTRO_FEATURES` znów są bez zmian.

Teraz, gdy licencja jest w distro, usuń `LICENSE_FLAGS_ACCEPTED` z fragmentu
`~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf` - akceptowane licencje to polityka, a nie płytka.

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "distro kurs na bazie poky-altcfg"

## Budowanie

    # kontener
    bitbake kurs-image

Obserwuj, co bitbake buduje. Prawie wszystko przychodzi z sstate, ale kilka recept buduje się od nowa.

**Q: Które recepty zbudowały się od nowa i dlaczego akurat te?**

<details>
<summary>Podpowiedź</summary>

Zmieniliśmy `DISTRO` i `DISTRO_NAME`. Poszukaj recept, które ich używają:

    # kontener
    grep -rln 'DISTRO_NAME\|${DISTRO}' /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/recipes-core/

</details>

## Na płytce

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1. Plik ma nową nazwę: `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.

Sprawdź, czy wszystko z poprzednich dni działa jak wcześniej:

    ls /usr/share/kurs/
    cat /usr/share/kurs/*
    systemctl status bme280-log
    lsmod | grep kurs
    cat /sys/class/leds/kurs/trigger

## Zadanie

Nazwa `Kurs Yocto` jest na płytce w kilku miejscach. Znajdź co najmniej trzy - w systemie i w logach startu na UART.

Źródła:
- [Yocto Dev Manual - Creating Your Own Distribution](https://docs.yoctoproject.org/wrynose/dev-manual/custom-distribution.html)
- [Yocto Reference - DISTRO](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTRO),
  [DISTRO_NAME](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTRO_NAME),
  [DISTROOVERRIDES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTROOVERRIDES),
  [DISTRO_FEATURES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DISTRO_FEATURES)
- [Yocto Dev Manual - Creating New Configuration Fragments](https://docs.yoctoproject.org/wrynose/dev-manual/creating-fragments.html)
  (fragmenty wbudowane `machine/` i `distro/`)
- meta-yocto wrynose `meta-poky/conf/distro/poky-altcfg.conf`, `poky.conf`
