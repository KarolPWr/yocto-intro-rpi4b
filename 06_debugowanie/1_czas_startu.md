# Czas startu systemu

Skracamy czas od podłączenia zasilania do `login:`. Zasada: **najpierw pomiar, potem zmiana, potem znowu pomiar**.
Każdy etap startu skracamy osobno:

    firmware RPi -> U-Boot -> kernel -> systemd (userspace) -> login:

Najpierw testujemy zmiany szybko na płytce (bez buildu), a potem zapisujemy w Yocto te, które coś dały.

Wymagania: U-Boot (ćwiczenie `03_uboot/1_uboot`), obraz `kurs-image` (`05_obraz_distro`).

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Narzędzie do pomiaru

`systemd-analyze` pokazuje, ile trwał start i które serwisy trwały najdłużej. W systemd jest osobną paczką i nie ma go
w naszym obrazie. Dopisz `systemd-analyze` do `RDEPENDS` w
`~/yocto-kurs/meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb` i zbuduj obraz:

    # kontener
    bitbake kurs-image

Paczka jest już zbudowana (recepta `systemd`), więc przebuduje się tylko packagegroup i obraz.

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1.

## Pomiar z UART

Do znaczników czasu na hoście użyjemy `ts` z pakietu `moreutils`:

    sudo apt install moreutils

Zamknij picocom (port może mieć otwarty tylko jeden program). Ustaw port i zapisuj wszystko, co przychodzi z UART,
z czasem od uruchomienia komendy:

    stty -F /dev/ttyUSB0 115200 raw -echo
    ts -s '%.s' < /dev/ttyUSB0 | tee start_1.log

Zaraz po uruchomieniu `ts` podłącz zasilanie. Gdy pojawi się `login:`, przerwij (`Ctrl+C`). Nie loguj się.

Uruchom płytkę jeszcze raz (odłącz i podłącz zasilanie) i zapisz drugi start do `start_2.log`. Porównaj:

    tail -3 start_1.log
    tail -3 start_2.log

Ostatnia linia z czasem to linia nad `login:`.

**Q: Dlaczego pierwszy start po wgraniu obrazu jest wolniejszy? Który start mierzyć?**

Znajdź w logu granice etapów:

    grep -E 'U-Boot 20|Starting kernel' start_2.log

- od zera do `U-Boot 20...` - firmware RPi (plus Twój czas reakcji przy zasilaniu - mierz 2-3 razy),
- od `U-Boot 20...` do `Starting kernel` - U-Boot.

## Pomiar w systemie

Zaloguj się (picocom albo `ssh root@<ADRES_IP>`):

    systemd-analyze time
    systemd-analyze blame | head -15
    systemd-analyze critical-chain

- `time` - czas kernela i userspace (do startu `systemd` i od startu do `multi-user.target`),
- `blame` - serwisy posortowane po czasie startu,
- `critical-chain` - łańcuch zależności, na który czekał `multi-user.target` (to on decyduje o czasie, a nie suma
  z `blame` - serwisy startują równolegle).

**Q: Którego etapu startu nie widać w `systemd-analyze time`?**

Wykres startu wszystkich serwisów:

    systemd-analyze plot > /tmp/start.svg

Na hoście:

    scp root@<ADRES_IP>:/tmp/start.svg .
    xdg-open start.svg

Przepisz wyniki do tabeli:

| Etap | Skąd | Przed | Po |
|---|---|---|---|
| firmware RPi | `ts`: do `U-Boot 20...` | | |
| U-Boot | `ts`: do `Starting kernel` | | |
| kernel | `systemd-analyze time` | | |
| userspace | `systemd-analyze time` | | |
| razem do `login:` | `ts`: ostatnia linia | | |

## Szybkie testy na płytce

Każdą zmianę sprawdź osobno: zmiana, restart, pomiar (`ts`, `systemd-analyze time`). Zmiana bez zysku nie trafia do
Yocto.

Partycja boot jest zamontowana w `/boot` - tam są `config.txt` (czyta firmware) i `cmdline.txt` (parametry kernela;
z U-Bootem też, bo U-Boot bierze je z device tree od firmware).

### Firmware RPi

Według dokumentacji meta-raspberrypi firmware czeka domyślnie sekundę przed załadowaniem kernela (u nas - U-Boota).
Na płytce:

    cat >> /boot/config.txt << 'EOF'
    boot_delay=0
    boot_delay_ms=0
    disable_splash=1
    EOF
    sync
    reboot

`disable_splash` wyłącza tęczowy ekran na HDMI.

### U-Boot

U-Boot odlicza 2 sekundy (`Hit any key to stop autoboot`). W picocomie zatrzymaj autostart i:

    setenv bootdelay 0
    saveenv
    boot

`bootdelay 0` - bez odliczania, ale wciśnięty klawisz nadal zatrzyma start. Jest też `-2` - bez sprawdzania
klawisza, ale wtedy nie dostaniesz się do konsoli U-Boota.

### Kernel

Kernel wypisuje na konsolę UART kilkaset linii. Parametr `quiet` zostawia tylko błędy (wszystko jest dalej
w `dmesg`). Na płytce:

    cat /boot/cmdline.txt
    sed -i 's/$/ quiet/' /boot/cmdline.txt
    cat /boot/cmdline.txt
    sync
    reboot

Sprawdź też, co kernel robi najdłużej. Dodaj `initcall_debug` (kernel zapisze czas każdej funkcji startowej):

    sed -i 's/$/ initcall_debug/' /boot/cmdline.txt
    sync
    reboot

Po starcie - 10 najwolniejszych:

    dmesg | grep 'initcall .* after' | sed -E 's/.*initcall (.*) returned .* after ([0-9]+) usecs/\2 \1/' | sort -n | tail -10

Wyrzucenie wolnych sterowników to zmiana konfiguracji kernela (pełny rebuild) - tego na kursie nie robimy.
Usuń `initcall_debug`:

    sed -i 's/ initcall_debug//' /boot/cmdline.txt
    sync
    reboot

### Userspace

Wróć do `systemd-analyze blame`. Na górze listy są m.in. serwisy, których nasza płytka nie potrzebuje. Przykład:
`ofono` obsługuje modemy GSM - na RPi 4B nie ma modemu.

    systemctl mask --now ofono.service
    reboot

`disable` usuwa tylko automatyczny start - serwis może nadal uruchomić inny serwis, socket albo D-Bus. `mask` blokuje
każdy start (link do `/dev/null`).

Nie maskuj `systemd-networkd` ani `dropbear` - stracisz SSH.

## Zmiany w Yocto

Zmiany z płytki znikną po następnym wgraniu obrazu. Zapisz w Yocto te, które dały zysk.

### Firmware i kernel

Wartości w `config.txt` i `cmdline.txt` ustawiają recepty `rpi-config` i `rpi-cmdline` z meta-raspberrypi. Zobacz,
jakie zmienne obsługują:

    # kontener
    grep -n -E 'BOOT_DELAY|DISABLE_SPLASH' /workdir/bitbake-builds/rpi4/layers/meta-raspberrypi/recipes-bsp/bootfiles/rpi-config_git.bb
    grep -n 'CMDLINE' /workdir/bitbake-builds/rpi4/layers/meta-raspberrypi/recipes-bsp/bootfiles/rpi-cmdline.bb

To ustawienia płytki - dopisz na końcu fragmentu `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf`:

    # szybszy start: firmware RPi bez czekania i bez ekranu startowego (config.txt)
    BOOT_DELAY = "0"
    BOOT_DELAY_MS = "0"
    DISABLE_SPLASH = "1"

    # szybszy start: kernel bez komunikatów na konsoli (cmdline.txt)
    CMDLINE_DEBUG = "quiet"

`CMDLINE_DEBUG` to w `rpi-cmdline.bb` miejsce na dodatkowe parametry kernela (domyślnie puste).

### U-Boot

Domyślne `bootdelay` pochodzi z konfiguracji U-Boota (`CONFIG_BOOTDELAY`). Środowisko domyślne zbudowanego U-Boota
jest w katalogu deploy:

    # kontener
    grep bootdelay /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/u-boot-initial-env

Recepta U-Boota łączy pliki `.cfg` z `SRC_URI` z konfiguracją domyślną - tak samo jak kernel w ćwiczeniu
`04_sprzet/2_kernel_config`. Utwórz:

    meta-kurs/recipes-bsp/u-boot/
    ├── files/bootdelay.cfg
    └── u-boot_%.bbappend

`files/bootdelay.cfg`:

    CONFIG_BOOTDELAY=0

`u-boot_%.bbappend`:

    FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

    SRC_URI += "file://bootdelay.cfg"

Jeśli masz już `u-boot_%.bbappend` z wcześniejszego ćwiczenia, dopisz tylko `file://bootdelay.cfg` do `SRC_URI`.

### Userspace

Ekran startowy `psplash` dodaje `core-image-base.bb` (`IMAGE_FEATURES += "splash"`). Maskowanie serwisów robimy na
gotowym rootfs, tak jak nazwę hosta w ćwiczeniu z obrazem. Dopisz w `~/yocto-kurs/meta-kurs/recipes-core/images/kurs-image.bb`
(pod `IMAGE_FEATURES += "ssh-server-dropbear"`):

    # bez ekranu startowego psplash
    IMAGE_FEATURES:remove = "splash"

i na końcu:

    # serwisy, których płytka nie potrzebuje
    KURS_MASKED_SERVICES = "ofono.service"

    kurs_mask_services() {
        for s in ${KURS_MASKED_SERVICES}; do
            systemctl --root=${IMAGE_ROOTFS} mask $s
        done
    }
    ROOTFS_POSTPROCESS_COMMAND += "kurs_mask_services"

`systemctl --root` działa na katalogu z rootfs, a nie na systemie, na którym działa - tak samo włącza serwisy sam
Yocto przy budowaniu obrazu.

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "szybszy start: firmware, U-Boot, kernel, serwisy"

### Budowanie

    # kontener
    bitbake kurs-image

Sprawdź przed wgraniem:

    # kontener
    cd /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/
    grep -E '^(boot_delay|disable_splash)' bootfiles/config.txt
    cat bootfiles/cmdline.txt
    grep bootdelay u-boot-initial-env
    ls -l $(bitbake-getvar -r kurs-image --value IMAGE_ROOTFS)/etc/systemd/system/ofono.service
    cd -

Wgraj obraz na kartę. Zmierz drugi start (`ts` i `systemd-analyze time`) i uzupełnij kolumnę „Po” w tabeli.

## Zadanie

Skróć jeszcze userspace. Przejdź przez `systemd-analyze blame` i `critical-chain` i zamaskuj w obrazie serwisy, których
płytka nie potrzebuje (SSH przez Ethernet i nasz `bme280-log` mają działać). Przy każdym zapisz jednym zdaniem, czym się zajmuje i czemu
go nie potrzebujemy. Zmierz wynik.

**Q: Lepiej byłoby tych programów w ogóle nie instalować. Skąd się biorą w obrazie i ile kosztowałoby ich usunięcie?**

<details>
<summary>Podpowiedź</summary>

Nie są w `IMAGE_INSTALL` wprost. Zobacz, co ciągnie `core-image-base` i od czego to zależy:

    # kontener
    bitbake-getvar -r kurs-image --value IMAGE_INSTALL
    less /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/recipes-core/packagegroups/packagegroup-base.bb

</details>

Źródła:
- [meta-raspberrypi - Extra build config](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/extra-build-config.md)
  (Boot delay, Disable splash screen), `recipes-bsp/bootfiles/rpi-config_git.bb`, `rpi-cmdline.bb`,
  `recipes-bsp/u-boot/u-boot_%.bbappend` (`CONFIG_BOOTDELAY=-2` dla RPi 5)
- [Raspberry Pi Documentation - config.txt: disable_splash](https://www.raspberrypi.com/documentation/computers/config_txt.html#disable_splash)
- [U-Boot - Environment Variables](https://docs.u-boot.org/en/latest/usage/environment.html) (`bootdelay`),
  U-Boot v2026.01 `boot/Kconfig` (`BOOTDELAY`)
- [Linux - kernel parameters](https://docs.kernel.org/admin-guide/kernel-parameters.html) (`quiet`, `initcall_debug`)
- [systemd-analyze(1)](https://www.freedesktop.org/software/systemd/man/latest/systemd-analyze.html),
  [systemctl(1)](https://www.freedesktop.org/software/systemd/man/latest/systemctl.html) (`mask`, `--root`)
- openembedded-core wrynose `meta/recipes-core/images/core-image-base.bb`, `meta/classes-recipe/rootfs-postcommands.bbclass`,
  `meta/recipes-bsp/u-boot/u-boot-configure.inc`, `meta/recipes-core/systemd/systemd_259.5.bb` (`systemd-analyze`)
- `moreutils` - `man ts`
