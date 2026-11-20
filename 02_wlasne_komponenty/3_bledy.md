# Typowe błędy i debugowanie recept

Psujemy receptę `hello` z poprzedniego ćwiczenia na kilka sposobów i szukamy przyczyny tak, jak przy prawdziwym
błędzie: komunikat -> log -> poprawka.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Literówka w SRC_URI

W `hello_1.0.bb` zmień linijkę z SRC_URI na:

    SRC_URI = "file://helo.c"

Zbuduj:

    # kontener
    bitbake hello

bitbake kończy już na parsowaniu:

    ERROR: .../hello_1.0.bb: Unable to get checksum for hello SRC_URI entry helo.c: file could not be found
    The following paths were searched:
    ...

**Q: W jakich katalogach i w jakiej kolejności bitbake szukał pliku?**

Lista to zawartość zmiennej `FILESPATH`. Wrócimy do niej w ćwiczeniu 5. Popraw literówkę.

## Błąd kompilacji

W `files/hello.c` usuń średnik po `printf(...)` i zbuduj:

    # kontener
    bitbake hello

Komunikat błędu podaje ścieżkę do logu taska:

    ERROR: hello-1.0-r0 do_compile: ...
    ERROR: Logfile of failure stored in: /workdir/.../hello/1.0/temp/log.do_compile.<pid>

Przeczytaj log (na hoście w dowolnym edytorze albo w kontenerze):

    # kontener
    cat $(bitbake-getvar -r hello --value T)/log.do_compile

`log.do_<task>` to dowiązanie do logu z ostatniego uruchomienia taska.

### Task bez bitbake

Obok logu leży skrypt, który bitbake uruchomił - `run.do_compile`. Możesz go uruchomić ręcznie, bez bitbake:

    # kontener
    cd $(bitbake-getvar -r hello --value T)
    ./run.do_compile

Tylko w kontenerze - skrypt ma w sobie ścieżki `/workdir/...`.

Popraw błąd „na szybko” w źródłach w `S` (`.../hello/1.0/sources/hello.c`) i uruchom `./run.do_compile` jeszcze raz.
W `B` pojawi się program `hello`.

To tylko szybki test: przy następnym `do_unpack` bitbake nadpisze `sources/` plikami z warstwy. Popraw błąd
w `files/hello.c` w warstwie i zbuduj:

    # kontener
    cd /workdir/bitbake-builds/rpi4/build
    bitbake hello

## bitbake -e

`bitbake -e` wypisuje wszystkie zmienne recepty po parsowaniu, razem z historią przypisań:

    # kontener
    bitbake -e hello | grep -B 10 "^S="

`bitbake-getvar`, którego używamy od dnia 1, robi to samo dla jednej zmiennej.

## Brak pamięci (OOM)

Na komputerze z 8 GB RAM build dużej recepty może zabić OOM killer - jądro hosta zabija proces, który zabrał
za dużo pamięci. W logu taska wygląda to na „dziwny” błąd kompilacji, np.:

    fatal error: Killed signal terminated program cc1plus

Prawdziwa przyczyna jest tylko w logu jądra. Sprawdź go na hoście (w kontenerze nie ma dostępu):

    sudo dmesg | grep -i -E "out of memory|killed process"

Naprawa: mniej równoległych zadań. W `local.conf` (to ustawienie Twojego komputera, a nie projektu), np.:

    BB_NUMBER_THREADS = "4"
    PARALLEL_MAKE = "-j 4"

`BB_NUMBER_THREADS` - ile tasków bitbake uruchamia naraz, `PARALLEL_MAKE` - ile procesów kompilacji w jednym tasku.

Źródła:
- [Yocto Dev Manual - Viewing Logs from Failed Tasks](https://docs.yoctoproject.org/wrynose/dev-manual/debugging.html#viewing-logs-from-failed-tasks)
- [Yocto Dev Manual - Viewing Variable Values](https://docs.yoctoproject.org/wrynose/dev-manual/debugging.html#viewing-variable-values)
- [Yocto Dev Manual - Limiting the Host Resources Usage](https://docs.yoctoproject.org/wrynose/dev-manual/limiting-resources.html)
