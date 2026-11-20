# Własny obraz i packagegroup

Fragment `meta-kurs/kurs` urósł: licencja, UART, SSH, Flask, nasze programy, U-Boot, overlaye, I2C. Wszystko to działa
globalnie - na każdą receptę i każdy obraz. Porządkujemy:
- **zawartość obrazu** (jakie paczki, jakie funkcje) - do recepty obrazu (to ćwiczenie),
- **polityka systemu** (init, licencje, nazwa) - do własnego distro (następne ćwiczenie),
- **płytka** (UART, U-Boot, I2C, overlaye) - zostaje we fragmencie.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Packagegroup

Packagegroup to recepta bez źródeł - jedna paczka, która w `RDEPENDS` ciągnie inne paczki. Zamiast wypisywać
programy w obrazie, dodajemy jedną nazwę.

Przykład z oe-core:

    # kontener
    cat /workdir/bitbake-builds/rpi4/layers/openembedded-core/meta/recipes-core/packagegroups/packagegroup-core-ssh-dropbear.bb

Na hoście utwórz `~/yocto-kurs/meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb`:

    SUMMARY = "Programy i narzędzia z kursu Yocto"

    inherit packagegroup

    RDEPENDS:${PN} = " \
        hello \
        nudoku \
        bme280-log \
        kurs-mod \
        i2c-tools \
        python3-flask \
    "

## Recepta obrazu

`~/yocto-kurs/meta-kurs/recipes-core/images/kurs-image.bb`:

    SUMMARY = "Obraz kursu Yocto na Raspberry Pi 4B"
    LICENSE = "MIT"

    # wszystko z core-image-base
    require recipes-core/images/core-image-base.bb

    # serwer SSH
    IMAGE_FEATURES += "ssh-server-dropbear"

    # nasze programy
    IMAGE_INSTALL += "packagegroup-kurs"

- `require` wkleja w tym miejscu całą receptę `core-image-base.bb` (ścieżka względem warstw z `BBPATH`),
- `IMAGE_FEATURES` - funkcje obrazu w recepcie obrazu. `EXTRA_IMAGE_FEATURES` zostaje dla `local.conf`
  i fragmentów (np. `root-login-with-empty-password`),
- `IMAGE_INSTALL +=` dopisuje do listy z `core-image.bbclass` (`IMAGE_INSTALL ?= ...` jest już ustawione
  w momencie `require`).

## Porządki we fragmencie

Usuń z `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf` wszystko, co trafiło do obrazu:
- `EXTRA_IMAGE_FEATURES += "ssh-server-dropbear"`,
- linie `IMAGE_INSTALL:append` (Flask, nasze programy, `i2c-tools`).

Popraw też opis fragmentu (`BB_CONF_FRAGMENT_DESCRIPTION`) - to teraz konfiguracja płytki.

Sprawdź, że `core-image-base` już nie ma naszych programów, a `kurs-image` - ma:

    # kontener
    bitbake-getvar -r core-image-base --value IMAGE_INSTALL
    bitbake-getvar -r kurs-image --value IMAGE_INSTALL
    bitbake-getvar -r kurs-image --value IMAGE_FEATURES

## Budowanie

    # kontener
    bitbake packagegroup-kurs
    bitbake kurs-image

Nowa nazwa obrazu - nowe nazwy plików w katalogu deploy:

    # kontener
    ls /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/kurs-image*

Porównaj listę paczek z obrazem z poprzednich dni:

    # kontener
    cd /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/
    diff <(cut -d' ' -f1 core-image-base-raspberrypi4-64.rootfs.manifest) \
         <(cut -d' ' -f1 kurs-image-raspberrypi4-64.rootfs.manifest)
    cd -

`core-image-base` w katalogu deploy to jeszcze obraz z poprzedniego buildu (z naszymi programami), więc różnic
powinno być niewiele.

**Q: Czy zawartość `kurs-image` jest taka sama jak `core-image-base` przed porządkami? Jeśli nie - co się zmieniło?**

Od teraz budujemy `kurs-image`. Obraz wgramy w następnym ćwiczeniu, razem z własnym distro. Zapisz zmiany w git
(na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-image: własny obraz i packagegroup-kurs"

## Zadanie

Płytka z naszym obrazem ma się przedstawiać jako `kurs` (`hostname`, prompt `root@kurs:~#`), a `core-image-base` -
dalej jako `raspberrypi4-64`. Zmień to tylko w recepcie `kurs-image`, bez ruszania cudzych recept.

<details>
<summary>Podpowiedź</summary>

Nazwa hosta jest w pliku `/etc/hostname`. Obraz może zmienić gotowy rootfs, zanim zrobi z niego `.wic` -
zobacz `ROOTFS_POSTPROCESS_COMMAND`.

</details>

Źródła:
- [Yocto Dev Manual - Customizing Images Using Custom .bb Files](https://docs.yoctoproject.org/wrynose/dev-manual/customizing-images.html#customizing-images-using-custom-bb-files)
- [Yocto Dev Manual - Customizing Images Using Custom Package Groups](https://docs.yoctoproject.org/wrynose/dev-manual/customizing-images.html#customizing-images-using-custom-package-groups)
- [Yocto Reference - packagegroup](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-packagegroup),
  [IMAGE_FEATURES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-IMAGE_FEATURES),
  [EXTRA_IMAGE_FEATURES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-EXTRA_IMAGE_FEATURES),
  [ROOTFS_POSTPROCESS_COMMAND](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-ROOTFS_POSTPROCESS_COMMAND)
