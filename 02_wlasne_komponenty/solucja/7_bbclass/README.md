# Rozwiązanie: własna klasa (.bbclass)

Pliki warstwy po ćwiczeniu (z Zadaniem):

    meta-kurs/
    ├── classes-recipe/kurs-info.bbclass
    ├── recipes-kernel/kurs-mod/kurs-mod_1.0.bb
    └── recipes-kurs/
        ├── bme280-log/bme280-log_1.0.bb
        ├── hello/hello_1.0.bb
        └── nudoku/nudoku_8.0.1.bb

Klasa: [meta-kurs/classes-recipe/kurs-info.bbclass](meta-kurs/classes-recipe/kurs-info.bbclass).
W receptach doszło `inherit kurs-info`.

## Odpowiedzi

**Task w `listtasks`:**

    do_kurs_info                    Zapisuje nazwę, wersję i licencję do pliku kurs-info

**Log taska** (`log.do_kurs_info` w `T`):

    DEBUG: Executing python function do_kurs_info
    NOTE: kurs-info: hello 1.0 MIT
    DEBUG: Python function do_kurs_info finished

**Pliki z klasy (przed Zadaniem):**

    hello 1.0 MIT
    nudoku 8.0.1 GPL-3.0-or-later
    bme280-log 1.0 MIT
    kurs-mod 1.0 GPL-2.0-only

## Zadanie - więcej informacji w pliku

Task w klasie:

    python do_kurs_info() {
        info = 'nazwa: ' + d.getVar('PN') + '\n'
        info += 'wersja: ' + d.getVar('PV') + '\n'
        info += 'licencja: ' + d.getVar('LICENSE') + '\n'
        # opis z recepty (bez SUMMARY bitbake.conf daje "<PN> version <PV>-<PR>")
        info += 'opis: ' + d.getVar('SUMMARY') + '\n'
        # nazwa warstwy z BBFILE_COLLECTIONS, ustawiana przez bitbake przy parsowaniu recepty
        info += 'warstwa: ' + d.getVar('FILE_LAYERNAME') + '\n'
        bb.note('kurs-info:\n' + info)
        with open(d.getVar('B') + '/kurs-info', 'w') as f:
            f.write(info)
    }

Pliki po zmianie:

    nazwa: hello
    wersja: 1.0
    licencja: MIT
    opis: Hello world w C - kurs Yocto
    warstwa: meta-kurs
    nazwa: nudoku
    wersja: 8.0.1
    licencja: GPL-3.0-or-later
    opis: Sudoku w terminalu (ncurses)
    warstwa: meta-kurs
    ...

`FILE_LAYERNAME` to nazwa z `BBFILE_COLLECTIONS` w `conf/layer.conf` warstwy (u nas `meta-kurs`), nie nazwa katalogu.
Ta sama nazwa działa jako override `layer-<nazwa>` (`OVERRIDES` w `bitbake.conf`).

Zmiana klasy zmienia sygnaturę `do_kurs_info`, a za nim `do_install` i dalszych tasków we wszystkich czterech
receptach - bitbake uruchamia je od nowa, ale nie kompiluje programów (`do_compile` bez zmian).

Źródła: openembedded-core wrynose `meta/conf/bitbake.conf` (`FILES:${PN}`, `SUMMARY`, `OVERRIDES`, `FILE_LAYERNAME ??=`),
`meta/classes-recipe/module.bbclass` (`FILES:${PN} = ""`), `meta/classes-global/utility-tasks.bbclass`
(`do_listtasks`, flaga `doc`); bitbake 2.18 `lib/bb/cookerdata.py` (`_parse_recipe` - `FILE_LAYERNAME`);
[BitBake manual - FILE_LAYERNAME](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-ref-variables.html#term-FILE_LAYERNAME).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/7_bbclass/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-info: wspólna klasa dla naszych recept"

W kontenerze:

    # kontener
    bitbake hello nudoku bme280-log kurs-mod
