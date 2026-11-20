# Rozwiązanie: własny obraz i packagegroup

Pliki warstwy po ćwiczeniu (z Zadaniem):

    meta-kurs/
    ├── conf/fragments/kurs.conf
    └── recipes-core/
        ├── images/kurs-image.bb
        └── packagegroups/packagegroup-kurs.bb

- Obraz: [meta-kurs/recipes-core/images/kurs-image.bb](meta-kurs/recipes-core/images/kurs-image.bb)
- Packagegroup: [meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb](meta-kurs/recipes-core/packagegroups/packagegroup-kurs.bb)
- Fragment po porządkach: [meta-kurs/conf/fragments/kurs.conf](meta-kurs/conf/fragments/kurs.conf) - zostały
  licencja (do distro w następnym ćwiczeniu) i ustawienia płytki.

## Odpowiedzi

**`kurs-image` a `core-image-base` sprzed porządków:** te same paczki, plus jedna nowa - `packagegroup-kurs`
(pusta paczka, która ciągnie nasze programy przez `RDEPENDS`). Nic nie zginęło - zmieniło się miejsce, w którym
opisujemy zawartość. `diff` manifestów powinien pokazać jedną linię:

    > packagegroup-kurs

Różnica w działaniu: wcześniej fragment dokładał nasze programy do **każdego** obrazu (`core-image-minimal` też).
Teraz tylko do `kurs-image`.

Źródła: openembedded-core wrynose `meta/classes-recipe/packagegroup.bbclass` (`PACKAGES = "${PN}"`,
`ALLOW_EMPTY`, `allarch`), `meta/classes-recipe/core-image.bbclass` (`IMAGE_INSTALL ?= "${CORE_IMAGE_BASE_INSTALL}"`,
`FEATURE_PACKAGES_ssh-server-dropbear`), `meta/recipes-core/images/core-image-base.bb`.

## Zadanie - nazwa hosta

W `kurs-image.bb`:

    # nazwa hosta tylko w tym obrazie (base-files ustawia ${MACHINE})
    kurs_hostname() {
        echo "kurs" > ${IMAGE_ROOTFS}${sysconfdir}/hostname
        sed -i 's/^127\.0\.1\.1 .*/127.0.1.1 kurs/' ${IMAGE_ROOTFS}${sysconfdir}/hosts
    }
    ROOTFS_POSTPROCESS_COMMAND += "kurs_hostname"

`ROOTFS_POSTPROCESS_COMMAND` - lista funkcji, które bitbake woła na gotowym rootfs (`IMAGE_ROOTFS`), zanim powstanie
`.wic`. Zmiana dotyczy tylko tego obrazu i nie przebudowuje żadnej paczki.

`base-files` wpisuje nazwę hosta (`hostname = "${MACHINE}"`) do `/etc/hostname` i do `/etc/hosts`
(`127.0.1.1 raspberrypi4-64`) - poprawiamy oba pliki.

Inne drogi i czemu nie:
- `hostname:pn-base-files = "kurs"` (np. w distro albo fragmencie) - zmienia paczkę `base-files`, więc każdy obraz
  dostanie nową nazwę, a nie tylko `kurs-image`,
- `bbappend` do `base-files` - to samo, plus cudza recepta.

Sprawdzenie bez flashowania:

    # kontener
    cat $(bitbake-getvar -r kurs-image --value IMAGE_ROOTFS)/etc/hostname

Źródła: openembedded-core wrynose `meta/recipes-core/base-files/base-files_3.0.14.bb` (`hostname`),
`meta/classes-recipe/rootfs-postcommands.bbclass`;
[Yocto Reference - ROOTFS_POSTPROCESS_COMMAND](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-ROOTFS_POSTPROCESS_COMMAND).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/05_obraz_distro/solucja/1_obraz/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-image: własny obraz i packagegroup-kurs"

W kontenerze:

    # kontener
    bitbake kurs-image
