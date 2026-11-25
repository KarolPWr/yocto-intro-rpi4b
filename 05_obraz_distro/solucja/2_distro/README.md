# Rozwiązanie: własne distro

Pliki po ćwiczeniu:

    meta-kurs/
    ├── conf/distro/kurs.conf
    └── conf/fragments/kurs.conf      (bez LICENSE_FLAGS_ACCEPTED)
    kurs.conf.json                    (oe-fragments: "distro/kurs")

- Distro: [meta-kurs/conf/distro/kurs.conf](meta-kurs/conf/distro/kurs.conf)
- Konfiguracja bitbake-setup: [kurs.conf.json](kurs.conf.json)

## Co gdzie jest po porządkach

| Miejsce | Co | U nas |
|---|---|---|
| distro `conf/distro/kurs.conf` | polityka całego systemu - wszystkie obrazy, wszystkie recepty | init (systemd z poky-altcfg), `DISTRO_FEATURES`, licencje, nazwa |
| obraz `kurs-image.bb` | zawartość jednego obrazu | `packagegroup-kurs`, SSH, nazwa hosta |
| fragment `meta-kurs/kurs` | ustawienia płytki | UART, U-Boot, overlaye, I2C |
| `kurs.conf.json` | co składa się na build | warstwy, rewizje, maszyna, distro, fragmenty |
| `local.conf` | szybkie eksperymenty, nie trzymamy tu nic na stałe | pusty |
| `site.conf` | ustawienia komputera | ścieżki do cache (`DL_DIR`, `SSTATE_DIR`) |

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/05_obraz_distro/solucja/2_distro/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cp ~/yocto-kurs/yocto-intro-rpi4b/05_obraz_distro/solucja/2_distro/kurs.conf.json ~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "distro kurs na bazie poky-altcfg"

W kontenerze:

    # kontener
    bitbake-setup update
    bitbake kurs-image

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1, plik `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.
