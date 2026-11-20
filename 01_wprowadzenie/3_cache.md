# Cache: downloads i sstate

Pełny build obrazu od zera to kilka godzin i kilkadziesiąt GB pobranych źródeł. Dlatego przed kursem dostałeś cache -
archiwum `yocto-kurs-cache.tar`. Rozpakujemy je do `~/yocto-kurs/cache` (w kontenerze: `/workdir/cache`). Ma dwa katalogi:

- **downloads** (`DL_DIR`) - pobrane źródła: archiwa `.tar.*`, klony repozytoriów git (`git2/`) itp.
- **sstate-cache** (`SSTATE_DIR`) - wyniki tasków z poprzednich buildów (shared state).

Ten sam cache można podpiąć do wielu buildów i projektów.

Źródła:
- [Yocto Overview - Shared State Cache](https://docs.yoctoproject.org/wrynose/overview-manual/concepts.html#shared-state-cache)
- [Yocto Reference - DL_DIR](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DL_DIR),
  [SSTATE_DIR](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-SSTATE_DIR)

## Rozpakowanie cache

Archiwum ma ok. 16 GB - po rozpakowaniu zajmie drugie tyle. Sprawdź, czy masz miejsce (na hoście):

    df -h ~/yocto-kurs

Archiwum leży w `~/Downloads`. W Ubuntu po polsku ten katalog to `~/Pobrane` - wtedy podmień ścieżkę w komendach.

Sprawdź, czy archiwum pobrało się w całości - porównaj jego sumę SHA-256 z sumą, którą wyliczyłem przy pakowaniu
(na hoście, potrwa chwilę):

    cd ~/Downloads
    echo "6b6b7adc49c9b56ecd106541aa9202da0a244a20e1577a6bb8f7ccabb93984d9  yocto-kurs-cache.tar" | sha256sum -c

Oczekiwany wynik:

    yocto-kurs-cache.tar: OK

Jeśli jest `FAILED` - archiwum jest uszkodzone albo niepełne, pobierz je jeszcze raz.

Rozpakuj archiwum (na hoście, potrwa kilka minut):

    mkdir -p ~/yocto-kurs/cache
    tar -xf ~/Downloads/yocto-kurs-cache.tar -C ~/yocto-kurs/cache

Sprawdź:

    ls ~/yocto-kurs/cache
    du -sh ~/yocto-kurs/cache/*

Powinieneś zobaczyć katalogi `downloads` (ok. 11 GB) i `sstate-cache` (ok. 5 GB). Archiwum możesz teraz usunąć,
żeby odzyskać miejsce:

    rm ~/Downloads/yocto-kurs-cache.tar

## Gdzie to jest ustawione

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

Sprawdź, gdzie bitbake trzyma teraz źródła i sstate:

    # kontener
    bitbake-getvar DL_DIR
    bitbake-getvar SSTATE_DIR

`bitbake-getvar` pokazuje wartość zmiennej i **plik, w którym ją ustawiono**.

Oba ustawienia są w `site.conf` w katalogu głównym, który utworzył `bitbake-setup`. W katalogu buildu jest do niego link:

    # kontener
    ls -l conf/site.conf
    cat conf/site.conf

`site.conf` to ustawienia wspólne dla wszystkich setupów w `bitbake-builds` - dobre miejsce na rzeczy zależne od komputera,
a nie od projektu (np. ścieżki do cache).

Bitbake wczytuje pliki konfiguracyjne z `conf/` w kolejności: `site.conf`, `auto.conf`, `toolcfg.conf`, `local.conf`
(źródło: `meta/conf/bitbake.conf` w openembedded-core).

**Q: Jeśli ta sama zmienna jest ustawiona przez `=` w `site.conf` i w `local.conf`, która wartość wygra?**

## Podpięcie cache

Teraz bitbake szukałby źródeł i sstate w pustych katalogach w `bitbake-builds`. Podpinamy nasz cache.

Na hoście otwórz w edytorze `~/yocto-kurs/bitbake-builds/site.conf` i zmień ścieżki:

    DL_DIR ?= "/workdir/cache/downloads"
    SSTATE_DIR ?= "/workdir/cache/sstate-cache"

**Uwaga:** ścieżki z perspektywy kontenera (`/workdir/...`), nie hosta - bitbake działa w kontenerze.

`BB_HASHSERVE_DB_DIR` zostaw bez zmian - wskazuje na `${SSTATE_DIR}`, więc przeniesie się razem z nim.

Sprawdź:

    # kontener
    bitbake-getvar DL_DIR
    bitbake-getvar SSTATE_DIR

## Zajrzyj do środka

    # kontener
    du -sh /workdir/cache/*
    ls /workdir/cache/downloads | head -20
    ls /workdir/cache/sstate-cache | head

Pliki sstate leżą w podkatalogach nazwanych od początku hasha. Nazwa pliku zawiera receptę, architekturę, wersję,
hash i nazwę taska, np. `sstate:<recepta>:...:<hash>_<task>.tar.zst`.

## Pytanie

Co się stanie, jeśli usuniesz `build/tmp` i zbudujesz obraz jeszcze raz? A co, jeśli usuniesz też `sstate-cache`?

**Nie usuwaj niczego** - to pytanie teoretyczne.
