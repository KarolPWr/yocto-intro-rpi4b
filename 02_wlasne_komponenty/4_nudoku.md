# Aplikacja open source: nudoku

[nudoku](https://github.com/jubalh/nudoku) - sudoku w terminalu (ncurses).
Szkic recepty wygeneruje `recipetool`, a my go sprawdzimy i poprawimy.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## recipetool

Źródła pobierzemy z repozytorium git, z commita oznaczonego tagiem `8.0.1` (wydanie 8.0.1). Commit taga
sprawdzisz na hoście:

    git ls-remote https://github.com/jubalh/nudoku refs/tags/8.0.1

Wygeneruj szkic recepty:

    # kontener
    mkdir -p /workdir/meta-kurs/recipes-kurs/nudoku
    recipetool create -o /workdir/meta-kurs/recipes-kurs/nudoku/nudoku_8.0.1.bb \
        -B master -S 96638addc1b5ec45293ed122ffb6b68248851115 \
        https://github.com/jubalh/nudoku.git

- `-B` - gałąź, na której jest commit,
- `-S` - commit (`SRCREV`); bez niego `recipetool` wziąłby najnowszy commit z gałęzi.

Archiwów `.../archive/...tar.gz` z GitHuba nie używamy: GitHub generuje je na żądanie i suma archiwum potrafi się
zmienić. W wrynose bitbake zgłasza je jako błąd QA `src-uri-bad`.

`recipetool` sklonował repozytorium, przejrzał źródła i zapisał szkic recepty `nudoku_8.0.1.bb`. Otwórz go na hoście
i znajdź, co narzędzie ustaliło samo:
- licencję (`LICENSE`, `LIC_FILES_CHKSUM`),
- źródła (`SRC_URI`, `SRCREV`) i wersję (`PV`),
- zależności (`DEPENDS`) i klasy (`inherit`) - z nich wynika, jak program jest budowany,
- notatki `# NOTE:` - tego narzędzie nie umiało rozstrzygnąć.

## Sprawdź, zanim zbudujesz

`recipetool` zgaduje. Rozpakuj źródła i porównaj szkic z tym, co w nich jest:

    # kontener
    bitbake -c unpack nudoku
    bitbake-getvar -r nudoku --value S

**Q: Na jakiej licencji jest nudoku? Czy `LICENSE` w szkicu się zgadza?**

<details>
<summary>Podpowiedź</summary>

Oprócz pliku `LICENSE` przeczytaj nagłówek licencji w `src/main.c`.

</details>

**Q: Czy nudoku potrzebuje biblioteki `cairo`?**

<details>
<summary>Podpowiedź</summary>

Zależności programu opisuje `configure.ac`: poszukaj `cairo` i sprawdź, kiedy jest używane.

</details>

Popraw receptę: usuń komentarze, które zostawił `recipetool`, popraw `LICENSE`, a `DEPENDS` zostaw tylko z tym,
czego program naprawdę potrzebuje. Usuń też linię `PV = "8.0.1+git"` - `SRCREV` to dokładnie wydanie 8.0.1, więc
wystarczy wersja z nazwy pliku.

## Build

    # kontener
    bitbake nudoku
    oe-pkgdata-util list-pkgs -p nudoku

Do obrazu dodaj nudoku i `hello` z ćwiczenia 2 - w `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf` popraw linię:

    IMAGE_INSTALL:append = " hello nudoku"

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "nudoku: recepta z recipetool"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1 i uruchom płytkę. Przez SSH:

    hello
    nudoku

Wyjście z nudoku: `Q`.

## Zadanie

Dodaj do kompilacji nudoku flagi ostrzeżeń `-Wall -Wextra`. Sprawdź w logu kompilacji, czy kompilator je dostał.

<details>
<summary>Podpowiedź</summary>

Flagi kompilatora C bitbake przekazuje w zmiennej `CFLAGS`.

</details>

Źródła:
- [Yocto Dev Manual - Creating the Base Recipe Using recipetool create](https://docs.yoctoproject.org/wrynose/dev-manual/new-recipe.html#creating-the-base-recipe-using-recipetool-create)
- [Yocto Dev Manual - Licensing](https://docs.yoctoproject.org/wrynose/dev-manual/new-recipe.html#licensing)
- [Yocto Reference - QA src-uri-bad](https://docs.yoctoproject.org/wrynose/ref-manual/qa-checks.html#qa-check-src-uri-bad)
- [Yocto Reference - autotools](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-autotools),
  [gettext](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-gettext),
  [pkgconfig](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-pkgconfig)
