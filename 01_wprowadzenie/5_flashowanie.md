# Wgranie obrazu i pierwszy boot

Wszystkie polecenia w tym ćwiczeniu wykonujemy **na hoście**, nie w kontenerze.

## Obraz

    cd ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64
    ls -l *.wic*

Potrzebujemy dwóch plików:
- `core-image-base-raspberrypi4-64.rootfs.wic.bz2` - spakowany obraz całej karty SD (partycja boot + rootfs),
- `core-image-base-raspberrypi4-64.rootfs.wic.bmap` - mapa bloków dla `bmaptool`.

`bmaptool` zapisuje na kartę tylko bloki, w których są dane (puste miejsce pomija), więc jest dużo szybszy niż `dd`.

Źródło: [Yocto Dev Manual - Flashing Images Using bmaptool](https://docs.yoctoproject.org/wrynose/dev-manual/bmaptool.html)

## Karta SD

Zanim włożysz kartę, uruchom podgląd logów kernela:

    sudo dmesg -w

Włóż kartę do czytnika i obserwuj nowe linie. Kernel wypisze nazwę urządzenia i rozmiar karty, np. dla czytnika USB:

    sd 6:0:0:0: [sdb] 62333952 512-byte logical blocks: (31.9 GB/29.7 GiB)
     sdb: sdb1 sdb2

albo dla czytnika wbudowanego w laptopa:

    mmcblk0: mmc0:aaaa SC32G 29.7 GiB
     mmcblk0: p1 p2

Nazwa (tu `sdb` albo `mmcblk0`) to Twoja karta - sprawdź, czy rozmiar się zgadza. Wyjście z `dmesg -w`: `Ctrl+C`.

Metoda alternatywna: `lsblk` przed włożeniem karty i po - nowe urządzenie na liście to karta.

**Uwaga:** zapis na złe urządzenie skasuje dane, np. na Twoim dysku. Jeśli nie masz pewności - zapytaj.

Ubuntu automatycznie montuje partycje z karty. Odmontuj je (podmień `<DEVICE>`, np. `sdb`):

    sudo umount /dev/<DEVICE>*

Wgraj obraz:

    sudo bmaptool copy core-image-base-raspberrypi4-64.rootfs.wic.bz2 /dev/<DEVICE>

`bmaptool` sam znajdzie plik `.bmap` leżący obok obrazu. Gdy skończy, wyjmij kartę i włóż do Raspberry Pi.

## Podłączenie UART

**Raspberry Pi odłączone od zasilania.**

Na konwerterze USB-UART ustaw napięcie **3.3V** (zworka, jeśli Twój konwerter ją ma). Piny GPIO Raspberry Pi
pracują na 3.3V - 5V może je uszkodzić.

| Konwerter USB-UART | Raspberry Pi (złącze GPIO) |
|---|---|
| GND | pin 6 (GND) |
| RX | pin 8 (GPIO14, TXD) |
| TX | pin 10 (GPIO15, RXD) |

TX jednej strony łączymy z RX drugiej. Pinu VCC konwertera nie podłączamy - płytka ma własne zasilanie.

Źródło: [Raspberry Pi Documentation - GPIO](https://www.raspberrypi.com/documentation/computers/raspberry-pi.html#gpio),
[Configure UARTs](https://www.raspberrypi.com/documentation/computers/configuration.html#configure-uarts)

## Pierwszy boot

Podłącz konwerter do komputera i otwórz konsolę:

    picocom -b 115200 /dev/ttyUSB0

Podłącz zasilanie Raspberry Pi. W picocomie powinny pojawić się logi kernela, a na końcu:

    raspberrypi4-64 login:

Zaloguj się jako `root`, bez hasła (to zasługa fragmentu z poprzedniego ćwiczenia).

Ta sama konsola jest też na monitorze podłączonym przez HDMI.

Wyjście z picocoma: `Ctrl+A`, potem `Ctrl+X`.

**Nic nie widać w picocomie?** Sprawdź po kolei: GND, czy TX i RX nie są zamienione, czy w `local.conf` jest
`ENABLE_UART = "1"`, czy otwierasz właściwy port (`ls /dev/ttyUSB*`).

## Sieć

Podłącz Raspberry Pi kablem Ethernet. Adres IP płytka dostanie z DHCP.

Na płytce (przez UART):

    ip a

Zapisz adres IP interfejsu `eth0` - przyda się w następnym ćwiczeniu.

## Zadanie

TODO: nie podoba mi się, może lepiej kazać rozszyfrować każdy parametr za pomocą google? 

Sprawdź na płytce, z jakimi parametrami wystartował kernel:

    cat /proc/cmdline

1. Który parametr odpowiada za konsolę na UART?
2. Skąd się wziął? Podpowiedź: zmienna, którą ustawiłeś w `local.conf`.
