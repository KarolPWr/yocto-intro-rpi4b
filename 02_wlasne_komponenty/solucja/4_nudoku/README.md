# Rozwiązanie: aplikacja open source - nudoku

Poprawiona recepta: [meta-kurs/recipes-kurs/nudoku/nudoku_8.0.1.bb](meta-kurs/recipes-kurs/nudoku/nudoku_8.0.1.bb)

## Odpowiedzi

**Licencja:** plik `LICENSE` to tekst GPL w wersji 3, więc `recipetool` wpisał `GPL-3.0-only`. Nagłówek w `src/main.c`
mówi jednak „either version 3 of the License, or (at your option) any later version”, czyli `GPL-3.0-or-later`.
`LIC_FILES_CHKSUM` (suma pliku `LICENSE`) zostaje bez zmian. Dlatego `recipetool` zostawia w szkicu ostrzeżenie,
że licencję trzeba sprawdzić samemu.

**cairo:** niepotrzebne. W `configure.ac` cairo jest opcją `--enable-cairo` (domyślnie wyłączoną) - służy tylko
do eksportu sudoku do PDF/PNG. Program wymaga tylko ncurses. Jeśli w szkicu jest notatka
`# NOTE: unable to map the following pkg-config dependencies: cairo` albo `cairo` w `DEPENDS` - usuwamy.
Z `cairo` w `DEPENDS` bitbake zbudowałby cairo z całym łańcuchem zależności, a program i tak by go nie użył.

`DEPENDS` to zależności czasu budowania (biblioteki i nagłówki potrzebne do kompilacji). Zależności czasu działania
(biblioteka `libncursesw` na płytce) bitbake dodaje sam - na podstawie bibliotek, z którymi program jest zlinkowany.

**Źródła i wersja:** `recipetool` wpisał `SRC_URI` z protokołem git (`git://...;protocol=https;branch=master`),
`SRCREV` z `-S` i `PV = "8.0.1+git"` - `+git` oznacza „jakiś commit po wydaniu 8.0.1”. Nasz `SRCREV` to dokładnie
tag `8.0.1`, więc `PV` usuwamy - wersja `8.0.1` z nazwy pliku jest prawdziwa. Przy źródłach z gita nie ma sumy
`sha256sum` - commit sam jednoznacznie opisuje zawartość.

**Klasy:**
- `autotools` - program buduje się przez `./configure && make && make install`; klasa robi to za nas, w tym
  `autoreconf` i budowanie cross-compilerem,
- `gettext` - tłumaczenia (katalog `po/`, `AM_GNU_GETTEXT` w `configure.ac`),
- `pkgconfig` - `configure` szuka ncurses przez `pkg-config`.

**Paczki:** `nudoku`, `nudoku-dbg`, `nudoku-doc` (strona `man`), `nudoku-locale-*` (tłumaczenia - każdy język osobno).

Źródła: [Yocto Dev Manual - Licensing](https://docs.yoctoproject.org/wrynose/dev-manual/new-recipe.html#licensing),
[Yocto Dev Manual - Dependencies](https://docs.yoctoproject.org/wrynose/dev-manual/new-recipe.html#dependencies),
nudoku 8.0.1: `LICENSE`, `src/main.c`, `configure.ac`;
openembedded-core wrynose `scripts/lib/recipetool/create.py` (`PV = "...+git"`), `meta/classes-global/insane.bbclass`
(`src-uri-bad` dla archiwów GitHuba).

## Zadanie

W recepcie:

    # dodatkowe ostrzeżenia kompilatora
    CFLAGS:append = " -Wall -Wextra"

Spacja na początku jest potrzebna - `:append` nie dodaje jej sam (ćwiczenie 8 z dnia 1). Sprawdzenie:

    # kontener
    bitbake nudoku
    grep -c -- "-Wextra" $(bitbake-getvar -r nudoku --value T)/log.do_compile

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/4_nudoku/meta-kurs/recipes-kurs ~/yocto-kurs/meta-kurs/

W `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf` linia z naszymi programami:

    IMAGE_INSTALL:append = " hello nudoku"

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "nudoku: recepta z recipetool"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
