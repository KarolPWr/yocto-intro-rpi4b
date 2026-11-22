# Rozwiązanie: czas startu systemu

Pliki warstwy po ćwiczeniu (z Zadaniem):

    meta-kurs/
    ├── conf/fragments/kurs.conf
    ├── recipes-bsp/u-boot/
    │   ├── files/bootdelay.cfg
    │   └── u-boot_%.bbappend
    └── recipes-core/
        ├── images/kurs-image.bb
        └── packagegroups/packagegroup-kurs.bb

- Fragment: [meta-kurs/conf/fragments/kurs.conf](meta-kurs/conf/fragments/kurs.conf) - firmware (`BOOT_DELAY`,
  `BOOT_DELAY_MS`, `DISABLE_SPLASH`) i kernel (`CMDLINE_DEBUG = "quiet"`)
- U-Boot: [meta-kurs/recipes-bsp/u-boot/u-boot_%.bbappend](meta-kurs/recipes-bsp/u-boot/u-boot_%.bbappend),
  [files/bootdelay.cfg](meta-kurs/recipes-bsp/u-boot/files/bootdelay.cfg)
- Obraz: [meta-kurs/recipes-core/images/kurs-image.bb](meta-kurs/recipes-core/images/kurs-image.bb) - bez `splash`,
  maskowane serwisy (z Zadaniem)
- Packagegroup: [meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb](meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb) - `systemd-analyze`

## Odpowiedzi

**Pierwszy start jest wolniejszy**, bo system kończy instalację:
- `run-postinsts.service` - skrypty instalacyjne paczek, których nie dało się wykonać przy budowaniu obrazu,
- dropbear generuje klucze hosta SSH,
- `boot.scr` zapisuje środowisko U-Boota (`saveenv`, jeśli na karcie nie ma `uboot.env`).

Mierzymy drugi i kolejne starty - tak startuje urządzenie u klienta. Pierwszy start warto zmierzyć osobno, jeśli
ważne jest pierwsze uruchomienie (np. na linii produkcyjnej).

**`systemd-analyze time` nie pokazuje firmware RPi ani U-Boota.** Kernel liczy czas od swojego startu, a firmware
RPi i U-Boot nie przekazują mu swoich czasów (na PC z UEFI `systemd-analyze` pokazuje też `firmware` i `loader`).
Dlatego te etapy mierzymy na UART.

**Ile daje `quiet`:** każdy znak na konsoli UART to 10 bitów (bit startu, 8 bitów danych, bit stopu), czyli przy
115200 bit/s ok. 11,5 kB/s. Kernel, który pisze na konsolę synchronicznie, czeka na UART. Oszacowanie na płytce
(przed dodaniem `quiet`):

    dmesg | wc -c

i podziel przez 11520 - wynik w sekundach to górna granica zysku. Ile naprawdę zyskaliśmy - pokazuje pomiar.

## Zadanie - kolejne serwisy

Serwisy włączone w `kurs-image` (z `blame`, `critical-chain` i `ls /etc/systemd/system/*.wants/`), których płytka
nie potrzebuje:

| Serwis | Czym się zajmuje | Czemu niepotrzebny |
|---|---|---|
| `ofono.service` | modemy GSM/LTE | brak modemu |
| `avahi-daemon.service`, `.socket` | mDNS (`kurs.local`), wykrywanie usług w sieci | łączymy się po IP |
| `bluetooth.service` | stos Bluetooth (BlueZ) | nie używamy Bluetooth |
| `hciuart.service` | podłącza układ Bluetooth na UART do kernela | j.w. |
| `rpcbind.service`, `.socket` | usługa potrzebna do NFS | nie montujemy NFS |

Same `.service` nie wystarczą tam, gdzie jest `.socket` - socket uruchomiłby serwis przy pierwszym połączeniu,
dlatego maskujemy oba.

Zostają m.in. `systemd-networkd` (sieć, SSH), `dropbear.socket` (SSH), `systemd-timesyncd` (czas z sieci - RPi nie
ma zegara RTC), `bme280-log`, `serial-getty@ttyS0` (konsola UART).

Do dyskusji: `busybox-syslog` i `busybox-klogd` dublują część pracy `systemd-journald`. Wyłączenie zmieni miejsce,
w którym są logi (`/var/log/messages` -> tylko `journalctl`) - to decyzja o produkcie, nie tylko o czasie startu.

**Skąd te programy w obrazie:** `core-image-base` instaluje `packagegroup-base-extended`
(`CORE_IMAGE_BASE_INSTALL` w `core-image.bbclass`), a ta ciągnie `packagegroup-base` i dalsze grupy zależnie od
`DISTRO_FEATURES` i `MACHINE_FEATURES`:

| Program | Grupa | Warunek |
|---|---|---|
| `bluez5` | `packagegroup-base-bluetooth` | `bluetooth` w `DISTRO_FEATURES` i w `MACHINE_FEATURES` |
| `avahi-daemon` | `packagegroup-base-zeroconf` | `zeroconf` w `DISTRO_FEATURES` |
| `rpcbind` | `packagegroup-base-nfs` | `nfs` w `DISTRO_FEATURES` |
| `ofono` | `packagegroup-base-3g` (w `packagegroup-base-extended`) | `3g` w `DISTRO_FEATURES`, maszyna bez `3g`, ale z `usbhost` (modem na USB) |

**Ile kosztuje usunięcie:**
- `ofono` (i `neard` - NFC, ta sama zasada) - tanio: tylko `packagegroup-base-extended` je dokłada. W recepcie obrazu
  zamiana `packagegroup-base-extended` na `packagegroup-base` (`IMAGE_INSTALL:remove` + `IMAGE_INSTALL +=`) zmienia
  tylko obraz, bez przebudowy paczek,
- reszta - zmiana `DISTRO_FEATURES` w distro, czyli przebudowa prawie całego systemu bez cache (ćwiczenie z distro),
  albo `MACHINE_FEATURES` (`bluetooth`) - przebudowa recept zależnych od maszyny. Na kursie tylko maskujemy, w produkcie
  - właściwa droga to distro z listą `DISTRO_FEATURES` dobraną od początku projektu.

`IMAGE_INSTALL:remove = "avahi-daemon"` nie zadziała - `avahi-daemon` nie jest w `IMAGE_INSTALL` wprost, tylko
w `RDEPENDS` packagegroupy, a tych menedżer paczek musi dociągnąć.

Źródła: openembedded-core wrynose `meta/recipes-core/packagegroups/packagegroup-base.bb`,
`meta/classes-recipe/core-image.bbclass`, `meta/conf/bitbake.conf` (`COMBINED_FEATURES`); meta-raspberrypi wrynose
`conf/machine/include/rpi-base.inc` (`MACHINE_FEATURES`); [systemctl(1)](https://www.freedesktop.org/software/systemd/man/latest/systemctl.html)
(`mask`).

## Czego nie robimy na kursie (pełny rebuild)

- odchudzenie kernela: sterowniki z `initcall_debug` jako moduły albo wyłączone, mniej opcji w konfiguracji,
- `DISTRO_FEATURES` dobrane do produktu (bez `bluetooth`, `nfs`, `zeroconf`, `3g`, `nfc`...),
- inne: rootfs tylko do odczytu, mniejszy obraz (mniej do wczytania z karty), kernel bez U-Boota (firmware RPi umie
  uruchomić kernel sam - tak było do dnia 3).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/06_czas_startu/solucja/1_czas_startu/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "szybszy start: firmware, U-Boot, kernel, serwisy"

Jeśli masz już `u-boot_%.bbappend` z ćwiczeń z U-Bootem, `cp` go nadpisze - dopisz z powrotem swoje pliki do `SRC_URI`.

W kontenerze:

    # kontener
    bitbake kurs-image

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1, plik `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.
