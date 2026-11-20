# Rozwiązanie: warstwy, meta-openembedded i serwer Flask

## Odpowiedzi

**Nothing RPROVIDES 'python3-flask':** obraz wymaga w czasie działania (runtime, stąd **R**PROVIDES) paczki
`python3-flask`, a żadna recepta w warstwach projektu jej nie dostarcza. Naprawa: dodać warstwę z tą receptą.

**Gdzie jest Flask:** recepta `python3-flask` jest w warstwie `meta-python`, a ta w repozytorium `meta-openembedded`
(`https://git.openembedded.org/meta-openembedded`). Layer Index pokazuje też zależności warstwy - `meta-python`
wymaga `meta-oe` (openembedded-layer).

**Layer 'meta-python' depends on layer 'openembedded-layer':** każda warstwa w `conf/layer.conf` deklaruje,
od jakich warstw zależy:

    LAYERDEPENDS_meta-python = "core (>= 12) openembedded-layer"

`openembedded-layer` to nazwa kolekcji warstwy `meta-oe`. Naprawa: dopisać `"meta-openembedded/meta-oe"`
do `bb-layers` w JSON.

Źródło: [Yocto Reference - LAYERDEPENDS](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-LAYERDEPENDS)

Błąd wychodzi już w `bitbake-setup update`, a nie dopiero w `bitbake`: `update` włącza fragmenty z JSON przez
`bitbake-config-build enable-fragment`, które czyta konfigurację wszystkich warstw.

Gotowy JSON: [kurs.conf.json](kurs.conf.json)

**Jak zapisywać stałe zmiany w konfiguracji:** nie w `local.conf`. `local.conf` to lokalny plik do eksperymentów -
nie jest wersjonowany, a `bitbake-setup update` może go wygenerować od nowa. Stałe zmiany trzymamy w miejscach
wersjonowanych w git:
- warstwy, ich wersje i włączone fragmenty - w JSON projektu (`kurs.conf.json`),
- ustawienia buildu - jako fragmenty konfiguracji we własnej warstwie, w konfiguracji distro albo w recepcie obrazu
  (własną warstwę zrobimy w kolejnych ćwiczeniach).

Mówi o tym nagłówek `local.conf` wygenerowany przez bitbake-setup:

    # If you would like to publish and share changes made to this file,
    # it is recommended to put them into a distro config, or to create
    # layer fragments from changes made here.

W `local.conf` (albo `site.conf`) zostaje to, co dotyczy tylko Twojego komputera, np. ścieżki do cache.

**Czy Flask jest w obrazie:** w manifeście jest linia `python3-flask <architektura> <wersja>`:

    grep flask ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/core-image-base-raspberrypi4-64.rootfs.manifest

## Zadanie [opcjonalne]

[app.py](app.py) - strona `/stats` zwraca JSON z temperaturą i uptime:

    {"temp_c": 47.2, "uptime_s": 312}

Flask zamienia słownik zwrócony z funkcji na odpowiedź JSON
([Flask - APIs with JSON](https://flask.palletsprojects.com/en/stable/quickstart/#apis-with-json)).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście podmień JSON w repo kursu:

    cp ~/yocto-kurs/yocto-intro-rpi4b/01_wprowadzenie/solucja/7_warstwy/kurs.conf.json ~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json

W kontenerze:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env
    bitbake-setup update --update-bb-conf yes

`update` wygenerował pusty `conf/local.conf`. Dopisz do niego [local.conf.dodatki](local.conf.dodatki) i zbuduj:

    # kontener
    cat /workdir/yocto-intro-rpi4b/01_wprowadzenie/solucja/7_warstwy/local.conf.dodatki >> conf/local.conf
    bitbake core-image-base

Jeśli w `conf/local.conf` zostały dodatki z ćwiczenia 4 (np. `update` nie zmienił `conf/`), nie dopisuj ich drugi raz -
wystarczy linia z `python3-flask`.
