# Eksploracja systemu

System działa. Teraz zaglądamy do środka: łączymy się po SSH, sprawdzamy, co trafiło do obrazu,
i czytamy informacje o sprzęcie prosto od kernela.

## SSH

Potrzebny będzie adres IP płytki z poprzedniego ćwiczenia (`ip a` na płytce przez UART).

Na hoście:

    ssh root@<ADRES_IP>

Logujemy się bez hasła - tak jak przez UART.

**Q: Co jest potrzebne na targecie, żeby można było połączyć się z płytką po SSH?**

### Po ponownym wgraniu obrazu

TODO: @claude mi to nie wyskoczyło, dorzuć do notatek jako pokaz a stąd wyrzuć 

Serwer SSH (dropbear) generuje klucze hosta przy pierwszym starcie systemu (`dropbearkey.service`).
Po każdym wgraniu obrazu na kartę płytka ma więc nowe klucze, a `ssh` na hoście ostrzega:

    WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!

Usuń stary klucz z listy znanych hostów i połącz się jeszcze raz:

    ssh-keygen -R <ADRES_IP>

### Kopiowanie plików

Pliki z hosta na płytkę kopiujemy przez `scp`, np.:

    scp plik.txt root@<ADRES_IP>:/tmp/

## Co jest w obrazie

Na płytce:

    cat /etc/os-release
    uname -a
    df -h
    free -m
    ps

Pełna lista paczek w obrazie jest w **manifeście** - pliku generowanym przy każdym buildzie obrazu. Na hoście:

    cd ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64
    cat core-image-base-raspberrypi4-64.rootfs.manifest

Każda linia to jedna paczka: `nazwa architektura wersja`.

    wc -l core-image-base-raspberrypi4-64.rootfs.manifest
    grep dropbear core-image-base-raspberrypi4-64.rootfs.manifest

Źródło: [Yocto Reference - IMAGE_MANIFEST](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-IMAGE_MANIFEST)

## Zadanie - statystyki systemu

1. Napisz skrypt w shellu, który co 20 sekund odczyta:
   - bieżącą temperaturę procesora,
   - taktowanie procesora,
   - uptime systemu,

   i dopisze je do pliku w `/var/log`.

   <details>
   <summary>Podpowiedź</summary>

   Szukaj w `/sys/class/thermal/`, `/sys/devices/system/cpu/cpu0/cpufreq/` i `/proc/uptime`.
   Sprawdź, w jakich jednostkach kernel podaje wartości.

   </details>

2. Zostaw skrypt na minutę, potem zrestartuj płytkę (`reboot`) i zajrzyj do pliku z logami.
   Co się stało? Dlaczego?
