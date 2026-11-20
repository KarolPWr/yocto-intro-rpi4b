# Rozwiązanie: własna warstwa

## Odpowiedzi

**Co ustawia `layer.conf`:**

| Linia | Znaczenie |
|---|---|
| `BBPATH .= ":${LAYERDIR}"` | bitbake szuka w tej warstwie plików `conf/` i `classes/` (np. fragmentów, klas) |
| `BBFILES += ".../recipes-*/*/*.bb ..."` | gdzie w warstwie leżą recepty i `.bbappend` |
| `BBFILE_COLLECTIONS += "meta-kurs"` | nazwa warstwy (kolekcji) - używana w `LAYERDEPENDS`, nazwach fragmentów, `show-layers` |
| `BBFILE_PATTERN_meta-kurs = "^${LAYERDIR}/"` | które pliki należą do tej warstwy |
| `BBFILE_PRIORITY_meta-kurs = "6"` | priorytet warstwy (`create-layer` domyślnie daje 6) |
| `LAYERDEPENDS_meta-kurs = "core"` | warstwa wymaga oe-core |
| `LAYERSERIES_COMPAT_meta-kurs = "wrynose"` | z którymi wydaniami Yocto warstwa działa |

Źródło: [Yocto Dev Manual - Creating Your Own Layer](https://docs.yoctoproject.org/wrynose/dev-manual/layers.html#creating-your-own-layer),
openembedded-core wrynose `meta/lib/bblayers/create.py`, `meta/lib/bblayers/templates/layer.conf`.

**Priorytet a wersja:** bitbake zbuduje `foo_1.0.bb` z warstwy B. Wyższy priorytet warstwy wygrywa niezależnie
od wersji (`PV`) recepty. Priorytety w naszym projekcie: `meta-raspberrypi` 9, `meta-kurs` 6, pozostałe 5.

Źródło: [Yocto Reference - BBFILE_PRIORITY](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-BBFILE_PRIORITY)

**Zadanie:** wartości są takie same jak przed przeniesieniem (`ssh-server-dropbear` w `IMAGE_FEATURES`,
`python3-flask` w `IMAGE_INSTALL`). Historia w `bitbake-getvar` wskazuje teraz plik
`.../layers/meta-kurs/conf/fragments/kurs.conf`, a nie `conf/local.conf`.

Pliki:
- [kurs.conf.json](kurs.conf.json) - JSON po ćwiczeniu,
- [meta-kurs/conf/fragments/kurs.conf](meta-kurs/conf/fragments/kurs.conf) - fragment.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env
    bitbake-layers create-layer /workdir/meta-kurs
    mkdir -p /workdir/meta-kurs/conf/fragments
    cp /workdir/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/1_wlasna_warstwa/meta-kurs/conf/fragments/kurs.conf /workdir/meta-kurs/conf/fragments/
    cp /workdir/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/1_wlasna_warstwa/kurs.conf.json /workdir/yocto-intro-rpi4b/kurs.conf.json
    bitbake-setup update --update-bb-conf yes

Na hoście:

    cd ~/yocto-kurs/meta-kurs
    git init
    git add .
    git commit -m "meta-kurs: nowa warstwa z fragmentem kurs"
