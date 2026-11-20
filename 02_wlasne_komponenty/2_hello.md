# Recepta hello world

Pierwsza własna recepta: program w C z jednego pliku, który leży w naszej warstwie.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Program i recepta

Na hoście utwórz katalog recepty:

    mkdir -p ~/yocto-kurs/meta-kurs/recipes-kurs/hello/files

Program - `~/yocto-kurs/meta-kurs/recipes-kurs/hello/files/hello.c`:

    #include <stdio.h>

    int main(void)
    {
        printf("Hello from Yocto!\n");

        return 0;
    }

Recepta - `~/yocto-kurs/meta-kurs/recipes-kurs/hello/hello_1.0.bb`:

    SUMMARY = "Hello world w C - kurs Yocto"
    LICENSE = "MIT"
    LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

    SRC_URI = "file://hello.c"

    S = "${UNPACKDIR}"

    do_compile() {
        ${CC} ${LDFLAGS} hello.c -o hello
    }

    do_install() {
        install -d ${D}${bindir}
        install -m 0755 hello ${D}${bindir}
    }

Zbuduj:

    # kontener
    bitbake hello

## Taski

Napisaliśmy tylko `do_compile` i `do_install`, a bitbake wykonał więcej tasków. Zobacz listę:

    # kontener
    bitbake -c listtasks hello

Pozostałe taski (`do_fetch`, `do_unpack`, `do_package`, ...) recepta dostała z klas, które bitbake dołącza
do każdej recepty.

## Gdzie co leży

    # kontener
    bitbake-getvar -r hello --value WORKDIR
    bitbake-getvar -r hello --value S
    bitbake-getvar -r hello --value B
    bitbake-getvar -r hello --value D
    bitbake-getvar -r hello --value T

Obejrzyj te katalogi na hoście (`/workdir` w kontenerze to `~/yocto-kurs` na hoście).

**Q: Co jest w `S`, co w `D`, a co w `T`?**

Na hoście sprawdź, dla jakiej architektury jest program:

    file ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/work/cortexa72-poky-linux/hello/1.0/image/usr/bin/hello

Dodatkowo za pomocą narzędzia oe-pkgdata-util można sprawdzić jakie paczki są tworzone przez receptę:

    # kontener
    oe-pkgdata-util list-pkgs -p hello
    oe-pkgdata-util list-pkg-files -p hello

## Szybki test na płytce

Program nie jest jeszcze w obrazie, ale możesz go od razu sprawdzić - skopiuj go na płytkę (z hosta):

    scp ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/work/cortexa72-poky-linux/hello/1.0/image/usr/bin/hello root@<ADRES_IP>:/tmp/
    ssh root@<ADRES_IP> /tmp/hello

## Do obrazu

Dopisz na końcu fragmentu `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf`:

    # nasze programy
    IMAGE_INSTALL:append = " hello"

i zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "hello: recepta hello world w C"

Obraz zbudujemy i wgramy w ćwiczeniu 4, razem z kolejnym programem.

Źródła:
- [Yocto Dev Manual - Building a Single .c File Package](https://docs.yoctoproject.org/wrynose/dev-manual/new-recipe.html#building-a-single-c-file-package)
- [Yocto Reference - S](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-S),
  [UNPACKDIR](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-UNPACKDIR),
  [D](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-D)
- [Yocto Dev Manual - Viewing Package Information with oe-pkgdata-util](https://docs.yoctoproject.org/wrynose/dev-manual/debugging.html#viewing-package-information-with-oe-pkgdata-util)
