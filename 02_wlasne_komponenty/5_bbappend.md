# bbappend: własny motd

Po zalogowaniu na płytkę widzisz ostrzeżenie „WARNING: Poky is a reference Yocto Project distribution...”.
To plik `/etc/motd` (message of the day). Zamienimy go na własny.

Pliku nie zmieniamy w oe-core. Dopisujemy do cudzej recepty plik `.bbappend` w naszej warstwie.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Skąd jest motd?

Plik `/etc/motd` instaluje recepta `base-files`:

    # kontener
    bitbake-layers show-recipes base-files
    bitbake-layers show-appends base-files

Do `base-files` jest już jeden `.bbappend` - z warstwy `meta-poky`. Zobacz go:

    # kontener
    cat /workdir/bitbake-builds/rpi4/layers/meta-yocto/meta-poky/recipes-core/base-files/base-files_%.bbappend
    find /workdir/bitbake-builds/rpi4/layers/meta-yocto/meta-poky/recipes-core/base-files

## Nasz bbappend

Na hoście utwórz w naszej warstwie:

    ~/yocto-kurs/meta-kurs/recipes-core/base-files/
    ├── base-files_%.bbappend
    └── files/
        └── motd

`base-files_%.bbappend`:

    FILESEXTRAPATHS:prepend := "${THISDIR}/files:"

Co tu jest:
- nazwa `base-files_%.bbappend` - dopisek do recepty `base-files` w dowolnej wersji (`%` zastępuje wersję),
- `FILESEXTRAPATHS` - dodatkowe katalogi, w których bitbake szuka plików z `SRC_URI`,
- `:prepend` - nasz katalog na początek listy,
- `:=` - `${THISDIR}` rozwinięty od razu, czyli katalog naszego `.bbappend` (przy `=` byłby to katalog recepty,
  która akurat jest parsowana).

`files/motd` - wpisz własny tekst, np.:

    Witam serdecznie

Zbuduj tylko `base-files` i sprawdź, co trafi na płytkę - bez budowania obrazu:

    # kontener
    bitbake base-files
    cat $(bitbake-getvar -r base-files --value D)/etc/motd

**Q: W `D` nadal jest `motd` z Poky. Dlaczego?**

<details>
<summary>Podpowiedź</summary>

Zobacz, gdzie bitbake szuka plików z `SRC_URI`. Zapisz całe środowisko recepty do pliku:

    # kontener
    bitbake -e base-files > log.bitbake

Otwórz `log.bitbake` i znajdź linię zaczynającą się od `FILESPATH=`. To lista katalogów rozdzielonych `:`.
bitbake sprawdza je po kolei i bierze pierwszy znaleziony plik. Znajdź na liście katalog `files/poky` z `meta-poky`
i nasz katalog `files`. Który jest wcześniej?

</details>

Napraw tak, żeby nasz `motd` wygrał, i sprawdź `D` jeszcze raz.

## Na płytce

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "base-files: własny motd"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1 i zaloguj się przez SSH.

Źródła:
- [Yocto Dev Manual - Appending Other Layers Metadata With Your Layer](https://docs.yoctoproject.org/wrynose/dev-manual/layers.html#appending-other-layers-metadata-with-your-layer)
- [Yocto Reference - FILESEXTRAPATHS](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESEXTRAPATHS),
  [FILESPATH](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESPATH),
  [FILESOVERRIDES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESOVERRIDES)
