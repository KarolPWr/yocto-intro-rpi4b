# Czas startu systemu

**Wyzwanie: kto najbardziej skróci czas startu systemu?**

Zasada: **najpierw pomiar, potem zmiana, potem znowu pomiar**. Start składa się z etapów, każdy skracamy osobno:

    firmware RPi -> U-Boot -> kernel -> systemd (userspace) -> login:

Wymagania: U-Boot (ćwiczenie `03_uboot/1_uboot`), obraz `kurs-image` (`05_obraz_distro`).

## Zasady wyzwania

- wynik: czas z `systemd-analyze time` (kernel + userspace), mediana z 3 startów, bez pierwszego startu po wgraniu,
- po starcie działa: logowanie na UART, SSH przez Ethernet i `bme280-log`,
- liczy się obraz zbudowany w Yocto i wgrany na kartę, a nie ręczne zmiany na płytce,
- bez pełnego rebuildu: nie zmieniamy `DISTRO_FEATURES`, distro, `MACHINE` ani konfiguracji kernela. Zmieniamy
  `config.txt`, `cmdline.txt`, zawartość obrazu `kurs-image` i pliki w rootfs.

Na koniec wpisz na czacie swój wynik i listę zmian.

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

## Pomiar

Uruchom płytkę, zaloguj się (picocom albo `ssh root@<ADRES_IP>`):

    systemd-analyze time
    systemd-analyze blame | head -15
    systemd-analyze critical-chain

- `time` - czas kernela i userspace (do startu `systemd` i od startu do `multi-user.target`),
- `blame` - serwisy posortowane po czasie startu,
- `critical-chain` - łańcuch zależności, na który czekał `multi-user.target` (to on decyduje o czasie, a nie suma
  z `blame` - serwisy startują równolegle).

Zrestartuj płytkę (`reboot`) i zmierz jeszcze raz.

**Q: Dlaczego pierwszy start po wgraniu obrazu jest wolniejszy? Który start mierzyć?**

Wykres startu wszystkich serwisów:

    systemd-analyze plot > /tmp/start.svg

Na hoście:

    scp root@<ADRES_IP>:/tmp/start.svg .
    xdg-open start.svg

Wszystkie komunikaty kernela i systemd z czasem od startu kernela - widać, gdzie są przerwy:

    journalctl -b -o short-monotonic

`systemd-analyze` liczy czas od startu kernela - firmware RPi i U-Boota nie widzi. Jak zmierzyć cały start, od
podłączenia zasilania - w suplemencie `1a_pomiar_uart.md`.

Przepisz wyniki do tabeli:

| Etap | Przed | Wynik |
|---|---|---|
| kernel | | |
| userspace | | |
| razem | | |

## Wyrzucamy U-Boota

U-Boot nie jest potrzebny, żeby RPi wystartował - firmware RPi sam ładuje kernel (tak startowaliśmy do dnia 3).
Ile kosztuje, widać w picocomie: wszystko od `U-Boot 20...` do `Starting kernel`.

Z U-Bootem plik `kernel8.img` na partycji boot to U-Boot, a kernel leży obok jako `Image`. Test na płytce - każ
firmware ładować od razu kernel:

    ls /boot
    echo "kernel=Image" >> /boot/config.txt
    sync
    reboot

W picocomie nie ma już U-Boota. Parametry kernela nadal pochodzą z `cmdline.txt` - firmware przekazuje je kernelowi
tak samo jak wcześniej U-Bootowi.

**Q: Co tracimy, wyrzucając U-Boota?**

W Yocto: U-Boota włączyliśmy w ćwiczeniu `03_uboot/1_uboot` we fragmencie
`~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf`. Usuń z niego:

    # bootloader U-Boot między firmware RPi a kernelem
    RPI_USE_U_BOOT = "1"

Sprawdź, co trafi na partycję boot:

    # kontener
    bitbake-getvar -r kurs-image --value IMAGE_BOOT_FILES

Bez `RPI_USE_U_BOOT` meta-raspberrypi kopiuje kernel jako `kernel8.img`, bez `u-boot.bin` i `boot.scr`. Kernel nie
skompiluje się od nowa - na `raspberrypi4-64` kernel to `Image` z U-Bootem i bez.

Nie buduj jeszcze - zbudujesz obraz raz, razem z resztą zmian z wyzwania.

Do ćwiczeń z U-Bootem (`03_uboot`) wrócisz, dopisując linię z powrotem.

## Co można skracać

Każdą zmianę sprawdź najpierw na płytce: zmiana, restart, pomiar. Zmiana bez zysku nie trafia do Yocto.

Partycja boot jest zamontowana w `/boot` - tam są `config.txt` (czyta firmware) i `cmdline.txt` (parametry kernela).
Jeśli płytka po zmianie nie wstanie, włóż kartę do czytnika na hoście i cofnij zmianę na partycji boot.

### Firmware RPi (`config.txt`)

- `dtoverlay=disable-wifi` - wyłącza Wi-Fi (łączymy się przez Ethernet): kernel nie ładuje sterownika ani firmware
  Wi-Fi,
- `boot_delay=0` i `boot_delay_ms=0` - według dokumentacji meta-raspberrypi firmware czeka domyślnie sekundę przed
  załadowaniem kernela, `disable_splash=1` - bez tęczowego ekranu na HDMI. Tych dwóch nie widać w `systemd-analyze`
  (zmierzysz je na UART - suplement).

W Yocto - zmienne recepty `rpi-config` albo `RPI_EXTRA_CONFIG` (ćwiczenie `04_sprzet/1_led_overlay`):

    # kontener
    grep -n -E 'BOOT_DELAY|DISABLE_SPLASH|RPI_EXTRA_CONFIG' /workdir/bitbake-builds/rpi4/layers/meta-raspberrypi/recipes-bsp/bootfiles/rpi-config_git.bb

### Kernel (`cmdline.txt`)

- `quiet` - kernel wypisuje na konsolę UART kilkaset linii, `quiet` zostawia tylko błędy (wszystko jest dalej
  w `dmesg`),
- `initcall_debug` - tylko do analizy: kernel zapisuje czas każdej funkcji startowej (też przy ładowaniu modułów).
  Po starcie - 10 najwolniejszych:

      dmesg | grep 'initcall .* after' | sed -E 's/.*initcall (.*) returned .* after ([0-9]+) usecs/\2 \1/' | sort -n | tail -10

- `modprobe.blacklist=<moduł>,<moduł>` - moduł się nie załaduje (lista załadowanych: `lsmod`),
- `initcall_blacklist=<funkcja>` - kernel pominie funkcję startową wbudowanego sterownika. Tylko dla sterowników,
  których płytka na pewno nie potrzebuje.

Parametry dopisujesz na końcu jedynej linii `cmdline.txt`, np.:

    sed -i 's/$/ quiet/' /boot/cmdline.txt

Nie usuwaj `console=serial0,115200` - stracisz logowanie przez UART.

W Yocto - zmienne recepty `rpi-cmdline`:

    # kontener
    grep -n 'CMDLINE' /workdir/bitbake-builds/rpi4/layers/meta-raspberrypi/recipes-bsp/bootfiles/rpi-cmdline.bb

### Userspace (systemd)

- serwisy, których płytka nie potrzebuje - szukaj w `systemd-analyze blame` i `critical-chain`. Przykład: `ofono`
  obsługuje modemy GSM - na RPi 4B nie ma modemu:

      systemctl mask --now ofono.service
      reboot

  `disable` usuwa tylko automatyczny start - serwis może nadal uruchomić inny serwis, socket albo D-Bus. `mask` blokuje
  każdy start (link do `/dev/null`). Nie maskuj `systemd-networkd` ani `dropbear` - stracisz SSH,
- ekran startowy `psplash` - dodaje go `core-image-base.bb` (`IMAGE_FEATURES += "splash"`),
- mniej programów w obrazie - patrz pytanie niżej.

W Yocto - maskowanie na gotowym rootfs, tak jak nazwa hosta w ćwiczeniu z obrazem (`ROOTFS_POSTPROCESS_COMMAND`
w `kurs-image.bb`).

<details>
<summary>Podpowiedź: maskowanie w obrazie</summary>

    kurs_mask_services() {
        systemctl --root=${IMAGE_ROOTFS} mask ofono.service
    }
    ROOTFS_POSTPROCESS_COMMAND += "kurs_mask_services"

`systemctl --root` działa na katalogu z rootfs, a nie na systemie, na którym działa - tak samo włącza serwisy sam
Yocto przy budowaniu obrazu.

</details>

**Q: Lepiej byłoby niepotrzebnych programów w ogóle nie instalować. Skąd się biorą w obrazie i ile kosztowałoby ich
usunięcie?**

<details>
<summary>Podpowiedź</summary>

Nie są w `IMAGE_INSTALL` wprost. Zobacz, co ciągnie `core-image-base` i od czego to zależy:

    # kontener
    bitbake-getvar -r kurs-image --value IMAGE_INSTALL
    less /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/recipes-core/packagegroups/packagegroup-base.bb

</details>

## Budowanie i wynik

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "szybszy start"

Zbuduj obraz:

    # kontener
    bitbake kurs-image

Sprawdź przed wgraniem, czy zmiany są na partycji boot:

    # kontener
    cd /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/
    cat bootfiles/config.txt | grep -v '^#' | grep -v '^$'
    cat bootfiles/cmdline.txt
    cd -

Wgraj obraz na kartę. Zmierz 3 starty po pierwszym (`systemd-analyze time`), uzupełnij kolumnę „Wynik” i wpisz
medianę na czacie.

Źródła:
- [meta-raspberrypi - Extra build config](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/extra-build-config.md)
  (Boot delay, Disable splash screen, Boot to U-Boot), `conf/machine/include/rpi-base.inc` (`IMAGE_BOOT_FILES`,
  `KERNEL_IMAGETYPE`), `conf/machine/raspberrypi4-64.conf`, `recipes-bsp/bootfiles/rpi-config_git.bb`, `rpi-cmdline.bb`
- [Raspberry Pi Documentation - config.txt](https://www.raspberrypi.com/documentation/computers/config_txt.html)
  (`kernel`, `disable_splash`, `dtoverlay`), [overlays/README](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/arch/arm/boot/dts/overlays/README)
  (`disable-wifi`)
- [Linux - kernel parameters](https://docs.kernel.org/admin-guide/kernel-parameters.html) (`quiet`, `initcall_debug`,
  `initcall_blacklist`), [modprobe.d(5)](https://man7.org/linux/man-pages/man5/modprobe.d.5.html) (`modprobe.blacklist`)
- [systemd-analyze(1)](https://www.freedesktop.org/software/systemd/man/latest/systemd-analyze.html),
  [systemctl(1)](https://www.freedesktop.org/software/systemd/man/latest/systemctl.html) (`mask`, `--root`),
  [journalctl(1)](https://www.freedesktop.org/software/systemd/man/latest/journalctl.html) (`short-monotonic`)
- openembedded-core wrynose `meta/recipes-core/images/core-image-base.bb`, `meta/classes-recipe/rootfs-postcommands.bbclass`,
  `meta/recipes-core/systemd/systemd_259.5.bb` (`systemd-analyze`)
