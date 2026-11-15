# Cache: downloads i sstate

Pełny build obrazu od zera to kilka godzin i kilkadziesiąt GB pobranych źródeł. Dlatego przed kursem dostałeś cache,
który rozpakowałeś do `~/yocto-kurs/cache` (w kontenerze: `/workdir/cache`). Ma dwa katalogi:

- **downloads** (`DL_DIR`) - pobrane źródła: archiwa `.tar.*`, klony repozytoriów git (`git2/`) itp.
- **sstate-cache** (`SSTATE_DIR`) - wyniki tasków z poprzednich buildów (shared state).

Ten sam cache można podpiąć do wielu buildów i projektów.

Źródła:
- [Yocto Overview - Shared State Cache](https://docs.yoctoproject.org/wrynose/overview-manual/concepts.html#shared-state-cache)
- [Yocto Reference - DL_DIR](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-DL_DIR),
  [SSTATE_DIR](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-SSTATE_DIR)

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
(źródło: `meta/conf/bitbake.conf` w openembedded-core).  TODO: Czyli local.conf nadpisuje inne? To tak działa?

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

Pliki sstate leżą w podkatalogach nazwanych od początku hasha. Nazwa pliku zawiera recepturę, architekturę, wersję,
hash i nazwę taska, np. `sstate:<receptura>:...:<hash>_<task>.tar.zst`.

## Zadanie

1. Znajdź w sstate-cache pliki należące do kernela (receptura `linux-raspberrypi`).  TODO: SPRAWDZ
2. Co się stanie, jeśli usuniesz `build/tmp` i zbudujesz obraz jeszcze raz? A co, jeśli usuniesz też `sstate-cache`?

**Nie usuwaj niczego** - to pytanie teoretyczne.
