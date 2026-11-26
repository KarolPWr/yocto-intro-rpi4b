# Rozwiązanie: czas startu systemu

Przykładowy wynik wyzwania. Pliki warstwy po ćwiczeniu:

    meta-kurs/
    ├── conf/fragments/kurs.conf
    └── recipes-core/
        ├── images/kurs-image.bb
        └── packagegroups/packagegroup-kurs.bb

- Fragment: [meta-kurs/conf/fragments/kurs.conf](meta-kurs/conf/fragments/kurs.conf) - bez `RPI_USE_U_BOOT`
  (bez U-Boota), firmware (`BOOT_DELAY`, `BOOT_DELAY_MS`, `DISABLE_SPLASH`) i kernel (`CMDLINE_DEBUG = "quiet"`)
- Obraz: [meta-kurs/recipes-core/images/kurs-image.bb](meta-kurs/recipes-core/images/kurs-image.bb) - bez `splash`,
  maskowane serwisy
- Packagegroup: [meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb](meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb) - `systemd-analyze`

## Odpowiedzi

**Pierwszy start jest wolniejszy**, bo system kończy instalację:
- `run-postinsts.service` - skrypty instalacyjne paczek, których nie dało się wykonać przy budowaniu obrazu,
- dropbear generuje klucze hosta SSH,
- `boot.scr` zapisuje środowisko U-Boota (`saveenv`, jeśli na karcie nie ma `uboot.env`).

Mierzymy drugi i kolejne starty - tak startuje urządzenie u klienta. Pierwszy start warto zmierzyć osobno, jeśli
ważne jest pierwsze uruchomienie (np. na linii produkcyjnej).

**Co tracimy bez U-Boota:**
- konsolę bootloadera - zmianę parametrów startu, start innego kernela albo z sieci (TFTP) bez flashowania karty,
- skrypty startowe (`boot.scr`) i środowisko (`uboot.env`) - logikę przed startem kernela,
- aktualizacje A/B z powrotem do starej wersji (np. RAUC, SWUpdate - licznik startów w U-Boocie),
- sprawdzanie podpisu kernela przed startem (FIT z podpisem).

Na RPi część tego da się zrobić w firmware (`tryboot`, `autoboot.txt` - wybór partycji boot), ale U-Boot jest
niezależny od płytki. W produkcie decyzja: zysk czasu kontra elastyczność startu i aktualizacji.

**Ile daje `quiet`:** każdy znak na konsoli UART to 10 bitów (bit startu, 8 bitów danych, bit stopu), czyli przy
115200 bit/s ok. 11,5 kB/s. Kernel, który pisze na konsolę synchronicznie, czeka na UART. Oszacowanie na płytce
(przed dodaniem `quiet`):

    dmesg | wc -c

i podziel przez 11520 - wynik w sekundach to górna granica zysku. Ile naprawdę zyskaliśmy - pokazuje pomiar.

## Wyzwanie - zmiany w Yocto

### Bez U-Boota

We fragmencie `kurs.conf` nie ma linii `RPI_USE_U_BOOT = "1"`. Na partycji boot `kernel8.img` to kernel
(`IMAGE_BOOT_FILES` z `rpi-base.inc`: `${KERNEL_IMAGETYPE};${SDIMG_KERNELIMAGE}`).

### Firmware i kernel

Wartości w `config.txt` i `cmdline.txt` ustawiają recepty `rpi-config` i `rpi-cmdline` z meta-raspberrypi. To
ustawienia płytki - na końcu fragmentu `kurs.conf`:

    # szybszy start: firmware RPi bez czekania i bez ekranu startowego (config.txt)
    BOOT_DELAY = "0"
    BOOT_DELAY_MS = "0"
    DISABLE_SPLASH = "1"

    # szybszy start: kernel bez komunikatów na konsoli (cmdline.txt)
    CMDLINE_DEBUG = "quiet"

`CMDLINE_DEBUG` to w `rpi-cmdline.bb` miejsce na dodatkowe parametry kernela (domyślnie puste) - tu trafiają też
`modprobe.blacklist=...` i `initcall_blacklist=...`, jeśli dały zysk, np.:

    CMDLINE_DEBUG = "quiet modprobe.blacklist=<moduł>"

Wyłączenie Wi-Fi - linia `dtoverlay` w `rpi-config_git.bbappend` z ćwiczenia `04_sprzet/1_led_overlay`:

    RPI_EXTRA_CONFIG:append = "\ndtoverlay=kurs-led\ndtoverlay=i2c-sensor,bme280\ndtoverlay=disable-wifi"

Overlay `disable-wifi.dtbo` jest już na partycji boot (`RPI_KERNEL_DEVICETREE_OVERLAYS` w `rpi-base.inc`).

### Userspace

Ekran startowy `psplash` dodaje `core-image-base.bb` (`IMAGE_FEATURES += "splash"`). W `kurs-image.bb`:

    # bez ekranu startowego psplash
    IMAGE_FEATURES:remove = "splash"

i maskowanie serwisów na gotowym rootfs:

    # serwisy, których płytka nie potrzebuje
    KURS_MASKED_SERVICES = "ofono.service ..."

    kurs_mask_services() {
        for s in ${KURS_MASKED_SERVICES}; do
            systemctl --root=${IMAGE_ROOTFS} mask $s
        done
    }
    ROOTFS_POSTPROCESS_COMMAND += "kurs_mask_services"

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
  tylko obraz, bez przebudowy paczek - mieści się w zasadach wyzwania,
- reszta - zmiana `DISTRO_FEATURES` w distro, czyli przebudowa prawie całego systemu bez cache (ćwiczenie z distro),
  albo `MACHINE_FEATURES` (`bluetooth`) - przebudowa recept zależnych od maszyny. W wyzwaniu tylko maskujemy, w produkcie
  właściwa droga to distro z listą `DISTRO_FEATURES` dobraną od początku projektu.

`IMAGE_INSTALL:remove = "avahi-daemon"` nie zadziała - `avahi-daemon` nie jest w `IMAGE_INSTALL` wprost, tylko
w `RDEPENDS` packagegroupy, a tych menedżer paczek musi dociągnąć.

Źródła: openembedded-core wrynose `meta/recipes-core/packagegroups/packagegroup-base.bb`,
`meta/classes-recipe/core-image.bbclass`, `meta/conf/bitbake.conf` (`COMBINED_FEATURES`); meta-raspberrypi wrynose
`conf/machine/include/rpi-base.inc` (`MACHINE_FEATURES`, `IMAGE_BOOT_FILES`, `RPI_KERNEL_DEVICETREE_OVERLAYS`);
[systemctl(1)](https://www.freedesktop.org/software/systemd/man/latest/systemctl.html) (`mask`);
[Raspberry Pi Documentation - autoboot.txt](https://www.raspberrypi.com/documentation/computers/config_txt.html#autoboot-txt)
(`tryboot`).

## Czego nie robimy w wyzwaniu (pełny rebuild)

- odchudzenie kernela: sterowniki z `initcall_debug` jako moduły albo wyłączone, mniej opcji w konfiguracji,
- `DISTRO_FEATURES` dobrane do produktu (bez `bluetooth`, `nfs`, `zeroconf`, `3g`, `nfc`...),
- inne: rootfs tylko do odczytu, mniejszy obraz (mniej do wczytania z karty).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/06_debugowanie/solucja/1_czas_startu/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "szybszy start"

W kontenerze:

    # kontener
    bitbake kurs-image

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1, plik `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.

Do ćwiczeń z U-Bootem (`03_uboot`) - dopisz z powrotem `RPI_USE_U_BOOT = "1"` we fragmencie.
