# Moduł jądra out-of-tree

Napiszemy własny moduł jądra - kod, który kernel ładuje w trakcie działania. 

Moduł jest
„out-of-tree”: jego źródła są w naszej warstwie, a nie w drzewie źródeł kernela.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Moduł i recepta

Na hoście utwórz w warstwie:

    ~/yocto-kurs/meta-kurs/recipes-kernel/kurs-mod/
    ├── kurs-mod_1.0.bb
    └── files/
        ├── kurs.c
        └── Makefile

`files/kurs.c`:

    #include <linux/module.h>
    #include <linux/moduleparam.h>

    // parametr podawany przy ładowaniu: insmod kurs.ko imie=Jan
    static char *imie = "Yocto";
    module_param(imie, charp, 0444);
    MODULE_PARM_DESC(imie, "imie do powitania");

    static int __init kurs_init(void)
    {
        pr_info("kurs: witaj, %s!\n", imie);
        return 0;
    }

    static void __exit kurs_exit(void)
    {
        pr_info("kurs: do widzenia, %s!\n", imie);
    }

    module_init(kurs_init);
    module_exit(kurs_exit);

    MODULE_LICENSE("GPL");
    MODULE_DESCRIPTION("Prosty modul jadra - kurs Yocto");

`files/Makefile` (wcięcia w liniach z `$(MAKE)` to tabulatory, nie spacje):

    obj-m := kurs.o

    SRC := $(shell pwd)

    all:
    	$(MAKE) -C $(KERNEL_SRC) M=$(SRC)

    modules_install:
    	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) modules_install

    clean:
    	$(MAKE) -C $(KERNEL_SRC) M=$(SRC) clean

`kurs-mod_1.0.bb`:

    SUMMARY = "Prosty moduł jądra - kurs Yocto"
    LICENSE = "GPL-2.0-only"
    LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

    inherit module

    SRC_URI = "file://Makefile \
               file://kurs.c \
              "

    S = "${UNPACKDIR}"

    # paczka z modułem nazywa się kernel-module-kurs-<wersja kernela>
    RPROVIDES:${PN} += "kernel-module-kurs"

Klasa `module` uruchamia nasz `Makefile` i podaje mu w `KERNEL_SRC` katalog ze zbudowanym kernelem. Moduł buduje się
dla dokładnie tego kernela, który jest w obrazie.

Zbuduj:

    # kontener
    bitbake kurs-mod

Do zbudowania modułu potrzebny jest katalog buildu kernela - mamy go po ćwiczeniu 2.

## Test na płytce

Znajdź moduł i paczkę, w której jest:

    # kontener
    oe-pkgdata-util list-pkgs -p kurs-mod
    find $(bitbake-getvar -r kurs-mod --value D) -name "*.ko"

Skopiuj `kurs.ko` na płytkę (z hosta; ścieżkę wziętą z `find` zacznij od `~/yocto-kurs` zamiast `/workdir`):

    scp ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/work/<...>/kurs.ko root@<ADRES_IP>:/tmp/

Na płytce:

    modinfo /tmp/kurs.ko
    insmod /tmp/kurs.ko imie=<Twoje imię>
    dmesg | tail -n 2
    lsmod | grep kurs
    cat /sys/module/kurs/parameters/imie
    rmmod kurs
    dmesg | tail -n 2

## Do obrazu

We fragmencie `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf` popraw linię z naszymi programami:

    IMAGE_INSTALL:append = " hello nudoku kurs-mod"

## Zadanie

Moduł ma się ładować sam przy starcie systemu, z parametrem `imie` ustawionym na Twoje imię. Wystarczą zmiany
w recepcie `kurs-mod`.

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-mod: moduł jądra z parametrem"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Wgraj obraz na kartę tak jak w ćwiczeniu 5 z dnia 1 i sprawdź:

    lsmod | grep kurs
    dmesg | grep kurs

Źródła:
- [Yocto Kernel Dev Manual - Incorporating Out-of-Tree Modules](https://docs.yoctoproject.org/wrynose/kernel-dev/common.html#incorporating-out-of-tree-modules)
- [Yocto Reference - module](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-module)
- [Yocto Reference - KERNEL_MODULE_AUTOLOAD](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-KERNEL_MODULE_AUTOLOAD),
  [KERNEL_MODULE_PROBECONF](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-KERNEL_MODULE_PROBECONF)
- [Linux kernel - Building External Modules](https://docs.kernel.org/kbuild/modules.html)
- openembedded-core wrynose `meta-skeleton/recipes-kernel/hello-mod/` - wzorcowa recepta modułu
