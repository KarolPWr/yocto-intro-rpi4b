# Własna klasa (.bbclass)

Klasa to kod wspólny dla wielu recept. Recepta dostaje go przez `inherit`. Nasza recepta nudoku ma
`inherit autotools`, więc nie pisaliśmy w niej `do_configure`, `do_compile` ani `do_install` - dała je klasa.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Klasy w oe-core

    # kontener
    ls /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/classes-recipe/
    ls /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/classes-global/
    ls /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/classes/

Katalog mówi, jak klasy wolno używać:
- `classes-recipe/` - tylko w recepcie (`inherit`),
- `classes-global/` - tylko globalnie, w konfiguracji (`INHERIT +=`), dla wszystkich recept naraz,
- `classes/` - bez określonego miejsca (oba sposoby).

Zobacz, które recepty z naszych warstw korzystają z klasy `systemd`:

    # kontener
    grep -rl "inherit.*systemd" /workdir/bitbake-builds/rpi4/layers/ | head

## Klasa kurs-info

Chcemy, żeby każdy nasz program zostawiał na płytce plik `/usr/share/kurs/<nazwa recepty>` z nazwą, wersją
i licencją. Ten sam kod w czterech receptach to kopiowanie - piszemy go raz, w klasie.

Na hoście utwórz `~/yocto-kurs/meta-kurs/classes-recipe/kurs-info.bbclass`:

    # kurs-info: plik /usr/share/kurs/<nazwa recepty> z nazwą, wersją i licencją

    # nowy task w Pythonie: zapisuje informacje do pliku w katalogu buildu
    python do_kurs_info() {
        info = d.getVar('PN') + ' ' + d.getVar('PV') + ' ' + d.getVar('LICENSE')
        bb.note('kurs-info: ' + info)
        with open(d.getVar('B') + '/kurs-info', 'w') as f:
            f.write(info + '\n')
    }
    addtask kurs_info after do_compile before do_install
    do_kurs_info[doc] = "Zapisuje nazwę, wersję i licencję do pliku kurs-info"

    # do_install z recepty (albo z innej klasy) + nasza linia na końcu
    do_install:append() {
        install -D -m 0644 ${B}/kurs-info ${D}${datadir}/kurs/${PN}
    }

    # /usr/share/kurs nie ma w domyślnych FILES paczki
    FILES:${PN}:append = " ${datadir}/kurs"

- `addtask` wstawia nowy task w kolejkę: po `do_compile`, przed `do_install`,
- `do_kurs_info[doc]` - flaga zmiennej: opis taska, który pokaże `listtasks`,
- `d.getVar()` - w funkcjach Pythona zmienne czytamy z `d` (cały stan bitbake'a dla tej recepty),
- `bb.note()` pisze do logu taska, nie na ekran.
- `FILES:${PN}:append`, a nie `+=` - klasa `module` (recepta `kurs-mod`) ustawia `FILES:${PN} = ""`.
  `+=` zadziała tylko wtedy, gdy `inherit kurs-info` stoi po `inherit module`, `:append` - zawsze
  (ćwiczenie 8 z dnia 1).

Dopisz do `~/yocto-kurs/meta-kurs/recipes-kurs/hello/hello_1.0.bb` (np. pod `SRC_URI`):

    inherit kurs-info

Zbuduj i sprawdź:

    # kontener
    bitbake hello
    bitbake -c listtasks hello | grep kurs
    cat $(bitbake-getvar -r hello --value T)/log.do_kurs_info
    oe-pkgdata-util list-pkg-files hello

## Wszystkie nasze recepty

Dopisz `inherit kurs-info` do recept `nudoku`, `bme280-log` i `kurs-mod`. Zbuduj je i sprawdź pliki:

    # kontener
    bitbake hello nudoku bme280-log kurs-mod
    for r in hello nudoku bme280-log kurs-mod; do
        cat $(bitbake-getvar -r $r --value D)/usr/share/kurs/$r
    done

Zwróć uwagę na nudoku: `do_install` daje mu klasa `autotools`, a nasz `:append` i tak się dokleja.

Obraz zbudujemy i wgramy w ćwiczeniu z własnym obrazem. Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-info: wspólna klasa dla naszych recept"

## Zadanie

Rozszerz klasę `kurs-info`: plik na płytce ma mieć każdą informację w osobnej linii, a do tego opis programu
i nazwę warstwy, z której pochodzi recepta. Dla `hello`:

    nazwa: hello
    wersja: 1.0
    licencja: MIT
    opis: Hello world w C - kurs Yocto
    warstwa: meta-kurs

Zbuduj nasze cztery recepty i sprawdź pliki w `D` (pętla wyżej).

<details>
<summary>Podpowiedź</summary>

Opis programu jest w zmiennej `SUMMARY`. Nazwę warstwy bitbake ustawia podczas parsowania recepty w zmiennej
`FILE_LAYERNAME` - sprawdź:

    # kontener
    bitbake-getvar -r hello --value FILE_LAYERNAME

</details>

Źródła:
- [BitBake manual - inherit Directive](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-metadata.html#inherit-directive),
  [Promoting a Function to a Task](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-metadata.html#promoting-a-function-to-a-task)
- [Yocto Reference - Classes](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html) (`classes-recipe/`, `classes-global/`, `classes/`)
- [Yocto Reference - FILES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILES),
  [SUMMARY](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-SUMMARY)
- [BitBake manual - FILE_LAYERNAME](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-ref-variables.html#term-FILE_LAYERNAME)
