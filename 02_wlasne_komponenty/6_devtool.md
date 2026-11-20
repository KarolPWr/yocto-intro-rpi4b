# devtool: zmiana w cudzym kodzie

Cel: Chcemy, żeby `nudoku -h` pokazywało w pomocy Twoje imię. 

To zmiana w kodzie źródłowym programu, którego nie jesteśmy
autorami - w Yocto robi się ją jako patch w naszej warstwie. `devtool` przygotuje źródła do edycji, wgra program
na płytkę bez flashowania karty i na końcu sam zrobi patch.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Źródła do edycji

    # kontener
    devtool modify nudoku

`devtool` rozpakował źródła nudoku do `build/workspace/sources/nudoku` - to repozytorium git. Od teraz bitbake
buduje nudoku z tego katalogu, a nie ze źródeł z `SRC_URI`.

Na hoście otwórz `~/yocto-kurs/bitbake-builds/rpi4/build/workspace/sources/nudoku/src/main.c`, znajdź funkcję
`print_usage()` i dopisz w niej linię z imieniem, np.:

    printf("Kurs Yocto: Jan Kowalski\n");

## Szybka pętla: build + deploy

    # kontener
    devtool build nudoku
    devtool deploy-target -c nudoku root@<ADRES_IP>

`deploy-target` kopiuje przez SSH wynik `do_install` na działającą płytkę. `-c` wyłącza sprawdzanie klucza hosta -
po każdym wgraniu obrazu płytka ma nowy klucz SSH.

Na płytce:

    nudoku -h

Poprawka, `devtool build`, `devtool deploy-target`, test - bez budowania obrazu i bez wyjmowania karty.

## Patch w warstwie

Zmiana działa na płytce, ale jest tylko w `workspace`. Zrobimy z niej patch w naszej warstwie. `devtool` robi patche
z commitów - zatwierdź zmianę w repozytorium źródeł (na hoście):

    cd ~/yocto-kurs/bitbake-builds/rpi4/build/workspace/sources/nudoku
    git status

Oprócz `src/main.c` zmieniony jest `po/Makevars`. Zmienił go build, nie my: `do_configure` klasy `autotools` dopisuje
do niego szablon gettext. Nie chcemy go w patchu, a `devtool finish` nie ruszy, dopóki w repozytorium są
niezatwierdzone zmiany. Cofnij go i zatwierdź swoją zmianę:

    git restore po/Makevars
    git add src/main.c
    git commit -m "nudoku: add name to help text (-h)"

W kontenerze:

    # kontener
    devtool finish nudoku /workdir/meta-kurs

Zobacz, co `devtool` zmienił w warstwie (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git status
    git diff

Zbuduj nudoku z recepty w warstwie:

    # kontener
    bitbake nudoku

Build kończy się błędem:

    ERROR: nudoku-8.0.1-r0 do_patch: QA Issue: Missing Upstream-Status in patch
    ...

**Q: Co oznacza ten błąd? Jak go naprawić?**

<details>
<summary>Podpowiedź</summary>

Link w komunikacie błędu prowadzi do opisu `Upstream-Status`. Otwórz patch w
`~/yocto-kurs/meta-kurs/recipes-kurs/nudoku/nudoku/` i dopisz w nagłówku, przed linią `---`:

    Upstream-Status: Inappropriate [kurs Yocto]

`Inappropriate` - zmiana tylko dla nas, nie wysyłamy jej do autorów nudoku.

</details>

Po poprawce zbuduj nudoku jeszcze raz i zapisz zmiany w git:

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "nudoku: patch z imieniem w pomocy"

Źródła:
- [Yocto Dev Manual - Use devtool modify to Modify the Source of an Existing Component](https://docs.yoctoproject.org/wrynose/dev-manual/devtool.html#use-devtool-modify-to-modify-the-source-of-an-existing-component)
- [Yocto Reference - devtool Quick Reference](https://docs.yoctoproject.org/wrynose/ref-manual/devtool-reference.html)
- [Yocto Contributor Guide - Patch Upstream Status](https://docs.yoctoproject.org/wrynose/contributor-guide/recipe-style-guide.html#patch-upstream-status)
