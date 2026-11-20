# Rozwiązanie: recepta hello world

Recepta: [meta-kurs/recipes-kurs/hello/](meta-kurs/recipes-kurs/hello/)

## Odpowiedzi

**S, D, T** (wszystkie w `WORKDIR` = `.../tmp/work/cortexa72-poky-linux/hello/1.0`):
- `S` = `${WORKDIR}/sources` (`UNPACKDIR`) - rozpakowane źródła, czyli `hello.c` z `files/`. `B` (katalog buildu)
  domyślnie jest równy `S`, więc tu leży też skompilowany `hello`,
- `D` = `${WORKDIR}/image` - wynik `do_install`: drzewo katalogów, które trafi na płytkę (`usr/bin/hello`),
- `T` = `${WORKDIR}/temp` - logi tasków (`log.do_compile`, ...) i skrypty, które bitbake uruchomił (`run.do_compile`, ...).

**Architektura:** `file` pokazuje `ELF 64-bit LSB ... ARM aarch64` - program zbudowany cross-compilerem dla RPi 4,
na hoście x86-64 się nie uruchomi.

**Paczki:** z jednej recepty powstaje kilka paczek - m.in. `hello` (program w `/usr/bin`) i `hello-dbg`
(informacje do debugowania). Do obrazu trafiają paczki z `IMAGE_INSTALL` i ich zależności - nie wszystkie paczki recepty.

Źródła: [Yocto Reference - WORKDIR](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-WORKDIR),
[B](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-B),
[T](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-T),
[Yocto Overview Manual - Package Splitting](https://docs.yoctoproject.org/wrynose/overview-manual/concepts.html#package-splitting)

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/2_hello/meta-kurs/recipes-kurs ~/yocto-kurs/meta-kurs/
    printf '\n# nasze programy\nIMAGE_INSTALL:append = " hello"\n' >> ~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "hello: recepta hello world w C"

W kontenerze:

    # kontener
    bitbake hello
