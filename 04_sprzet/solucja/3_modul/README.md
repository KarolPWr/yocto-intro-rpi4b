# Rozwiązanie: moduł jądra out-of-tree

Recepta (z Zadaniem): [meta-kurs/recipes-kernel/kurs-mod/](meta-kurs/recipes-kernel/kurs-mod/)

    meta-kurs/recipes-kernel/kurs-mod/
    ├── files/
    │   ├── kurs.c
    │   └── Makefile
    └── kurs-mod_1.0.bb

## Odpowiedzi

**Paczki i ścieżka:** `oe-pkgdata-util list-pkgs -p kurs-mod` pokazuje m.in. `kurs-mod` i
`kernel-module-kurs-<wersja kernela>`. Moduł leży w `D` w `usr/lib/modules/<wersja kernela>/updates/kurs.ko` -
`updates/` to domyślny katalog kernela dla modułów spoza drzewa (`INSTALL_MOD_DIR`).

Źródła: [Linux kernel - Building External Modules](https://docs.kernel.org/kbuild/modules.html);
[Yocto Kernel Dev Manual - Incorporating Out-of-Tree Modules](https://docs.yoctoproject.org/wrynose/kernel-dev/common.html#incorporating-out-of-tree-modules);
kernel 6.18 `scripts/Makefile.modinst` (`INSTALL_MOD_DIR ?= updates`).

## Zadanie - ładowanie przy starcie

Na końcu `kurs-mod_1.0.bb`:

    # ładowanie modułu przy starcie systemu, z parametrem
    KERNEL_MODULE_AUTOLOAD += "kurs"
    KERNEL_MODULE_PROBECONF += "kurs"
    module_conf_kurs = "options kurs imie=Jan"

- `KERNEL_MODULE_AUTOLOAD` - bitbake tworzy plik `/usr/lib/modules-load.d/kurs.conf` z nazwą modułu; przy starcie
  systemd (`systemd-modules-load`) ładuje moduły z tych plików,
- `KERNEL_MODULE_PROBECONF` + `module_conf_kurs` - plik `/usr/lib/modprobe.d/kurs.conf` z linią `options ...`;
  `modprobe` bierze z niego parametry przy każdym ładowaniu modułu.

Z `sysvinit` te pliki trafiłyby do `/etc/modules-load.d/` i `/etc/modprobe.d/`.

Sprawdzenie na płytce:

    cat /usr/lib/modules-load.d/kurs.conf
    cat /usr/lib/modprobe.d/kurs.conf
    dmesg | grep kurs

Źródła: [Yocto Reference - KERNEL_MODULE_AUTOLOAD](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-KERNEL_MODULE_AUTOLOAD),
[KERNEL_MODULE_PROBECONF](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-KERNEL_MODULE_PROBECONF);
openembedded-core wrynose `meta/classes-recipe/kernel-module-split.bbclass` (`modulesloaddir`, `modprobedir`).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/04_sprzet/solucja/3_modul/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-mod: moduł jądra z parametrem"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
