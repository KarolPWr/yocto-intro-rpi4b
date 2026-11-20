# 0. Przygotowanie przed kursem

Do zrobienia **przed pierwszym dniem kursu**, najlepiej kilka dni wcześniej. Zajmie ok. 30-60 minut,
z czego większość to pobieranie i rozpakowanie cache.

Jeśli cokolwiek nie działa - napisz do mnie.

Zakładam, że masz już za sobą kroki z dokumentu „Przygotowanie przed szkoleniem” (Ubuntu 24.04,
Docker bez sudo, sprawdzona płytka z Raspberry Pi OS).

## 1. Katalog roboczy

Cały kurs robimy w jednym katalogu. Ten katalog będzie podmontowany do kontenera jako `/workdir`.

    mkdir ~/yocto-kurs
    cd ~/yocto-kurs

Sprawdź system plików i wolne miejsce:

    df -hT ~/yocto-kurs

Wymagania:
- typ systemu plików: `ext4`, `btrfs` albo `xfs`. **Nie** NTFS/exFAT (np. dysk zewnętrzny sformatowany pod Windowsa) -
  build skończy się błędami uprawnień,
- wolne miejsce: min. **120 GB**.

Źródło: [Yocto FAQ - Why do I get chmod permission issues?](https://docs.yoctoproject.org/wrynose/ref-manual/faq.html)

## 2. RAM i swap

    free -h
    nproc

Yocto jest pamięciożerne - przy kompilacji wielu rzeczy równolegle 8 GB RAM potrafi się skończyć,
a wtedy system ubija proces kompilatora (OOM) i build pada.

Jeśli masz **mniej niż 16 GB RAM**, dodaj swap (plik wymiany):

    sudo fallocate -l 8G /swapfile-yocto
    sudo chmod 600 /swapfile-yocto
    sudo mkswap /swapfile-yocto
    sudo swapon /swapfile-yocto
    echo '/swapfile-yocto none swap sw 0 0' | sudo tee -a /etc/fstab

Sprawdź, czy swap jest aktywny:

    swapon --show

Po kursie możesz go usunąć: `sudo swapoff /swapfile-yocto`, usuń linię z `/etc/fstab`, `sudo rm /swapfile-yocto`.

## 3. Obraz kontenera

Na kursie budujemy w kontenerze [crops/poky](https://github.com/crops/poky-container) - każdy ma identyczne
środowisko, niezależnie od tego, co jest zainstalowane na hoście. Pobierz obraz teraz (ok. 1 GB):

    docker pull crops/poky:ubuntu-22.04

Ubuntu 22.04 w kontenerze to jeden z oficjalnie wspieranych systemów dla Yocto 6.0 Wrynose
([System Requirements](https://docs.yoctoproject.org/wrynose/ref-manual/system-requirements.html#supported-linux-distributions)).

Szybki test - kontener powinien się uruchomić i od razu zakończyć:

    docker run --rm -v ~/yocto-kurs:/workdir crops/poky:ubuntu-22.04 --workdir=/workdir bash -c 'id && python3 --version'

Oczekiwany wynik (numery mogą się różnić):

    uid=1000(pokyuser) gid=1000(pokyuser) groups=1000(pokyuser)
    Python 3.10.12

## 4. Cache: downloads + sstate

Ode mnie dostajesz archiwum z cache. Zawiera dwa katalogi:
- `downloads/` - pobrane źródła wszystkich paczek (żeby nie ściągać ich z internetu),
- `sstate-cache/` - wyniki wcześniejszego buildu (żeby nie kompilować wszystkiego od zera).

Co dokładnie siedzi w środku i jak bitbake z tego korzysta - omówimy pierwszego dnia.

Pobierz archiwum `<NAZWA_ARCHIWUM>` (ok. `<ROZMIAR>` GB) z linku, który ode mnie dostaniesz,
razem z plikiem `<NAZWA_ARCHIWUM>.sha256`. Oba pliki zapisz w `~/yocto-kurs`.

Sprawdź, czy archiwum pobrało się w całości:

    cd ~/yocto-kurs
    sha256sum -c <NAZWA_ARCHIWUM>.sha256

Oczekiwany wynik: `<NAZWA_ARCHIWUM>: OK`. Jeśli jest `FAILED` - pobierz jeszcze raz.

Rozpakuj:

    mkdir -p ~/yocto-kurs/cache
    tar -xf <NAZWA_ARCHIWUM> -C ~/yocto-kurs/cache

Sprawdź:

    ls ~/yocto-kurs/cache
    du -sh ~/yocto-kurs/cache/*

Powinieneś zobaczyć katalogi `downloads` i `sstate-cache`. Archiwum możesz teraz usunąć,
żeby odzyskać miejsce.

## 5. Narzędzia na hoście

Dwa programy, których użyjemy pierwszego dnia - do wgrywania obrazu na kartę SD i do konsoli szeregowej:

    sudo apt install bmap-tools picocom

Żeby otwierać port szeregowy bez `sudo`, dodaj się do grupy `dialout`:

    sudo usermod -aG dialout $USER

Zmiana grupy działa po ponownym zalogowaniu.

## 6. Konwerter USB-UART

Podłącz konwerter USB-UART do komputera (bez płytki) i sprawdź, czy system go wykrył:

    sudo dmesg | tail
    ls -l /dev/ttyUSB*

Powinno być widać urządzenie `/dev/ttyUSB0` (przy FTDI w logu pojawi się `FTDI USB Serial Device converter now attached to ttyUSB0`).

Sprawdź, czy otworzysz port bez `sudo`:

    picocom -b 115200 /dev/ttyUSB0

Wyjście z picocoma: `Ctrl+A`, potem `Ctrl+X`.

Jeśli dostajesz `Permission denied` - nie zadziałała zmiana grupy z punktu 5 (wyloguj się i zaloguj ponownie).

Podłączenie konwertera do Raspberry Pi zrobimy razem na kursie.

## 7. [OPCJONALNIE] Wyślij mi wynik

Odpal i wklej mi wynik (mailem albo na czacie):

    echo "CPU: $(nproc)"; free -h; swapon --show; df -hT ~/yocto-kurs; ls ~/yocto-kurs/cache; docker --version

## Checklista

- [ ] `~/yocto-kurs` na `ext4`/`btrfs`/`xfs`, min. 120 GB wolnego
- [ ] RAM < 16 GB → swap dodany i aktywny
- [ ] `docker pull crops/poky:ubuntu-22.04` zrobiony, test kontenera przechodzi
- [ ] cache rozpakowany w `~/yocto-kurs/cache` (`downloads` + `sstate-cache`)
- [ ] `bmap-tools` i `picocom` zainstalowane, jestem w grupie `dialout`
- [ ] konwerter USB-UART widoczny jako `/dev/ttyUSB0`, picocom otwiera port bez `sudo`
- [ ] wynik z punktu 7 
