# Własna warstwa

Wszystko, co sami dodajemy do systemu (recepty, zmiany w cudzych receptach, konfigurację), trzymamy we własnej
warstwie. Warstw pobranych przez `bitbake-setup` (oe-core, meta-raspberrypi, meta-openembedded) nie zmieniamy -
przy aktualizacji zostałyby nadpisane wersją z repozytorium.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Nowa warstwa

Warstwę `meta-kurs` tworzymy obok `bitbake-builds`, a nie w `layers/` - to nasz kod, z własnym repozytorium git:

    # kontener
    bitbake-layers create-layer /workdir/meta-kurs

Narzędzie podpowiada `bitbake-layers add-layer` - **nie rób tego**. `add-layer` dopisuje warstwę do
`conf/bblayers.conf`, a ten plik `bitbake-setup update` generuje od nowa (ćwiczenie 7 z dnia 1). Warstwę dodamy przez JSON.

Zobacz, co powstało (na hoście):

    ls ~/yocto-kurs/meta-kurs
    cat ~/yocto-kurs/meta-kurs/conf/layer.conf


Warstwa to kod - wersjonujemy ją od początku. Na hoście (w kontenerze nie ma Twojej konfiguracji git):

    cd ~/yocto-kurs/meta-kurs
    git init
    git add .
    git commit -m "meta-kurs: nowa warstwa"

## Konfiguracja z local.conf do warstwy

W ćwiczeniu 7 `bitbake-setup update` wygenerował `local.conf` od nowa i ustawienia trzeba było przywracać z backupu.
Przeniesiemy je do warstwy - jako fragment konfiguracji, taki sam jak `root-login-with-empty-password` z oe-core.

Utwórz na hoście `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf`:

    BB_CONF_FRAGMENT_SUMMARY = "Konfiguracja obrazu na kurs Yocto (RPi 4B)"
    BB_CONF_FRAGMENT_DESCRIPTION = "Konsola UART, serwer SSH (dropbear), licencja firmware WiFi/BT, Flask."

    # akceptacja licencji firmware WiFi/BT (linux-firmware-rpidistro)
    LICENSE_FLAGS_ACCEPTED = "synaptics-killswitch"

    # konsola szeregowa na pinach GPIO (UART)
    ENABLE_UART = "1"

    # serwer SSH
    EXTRA_IMAGE_FEATURES += "ssh-server-dropbear"

    # serwer WWW we Flasku
    IMAGE_INSTALL:append = " python3-flask"

`BB_CONF_FRAGMENT_SUMMARY` i `BB_CONF_FRAGMENT_DESCRIPTION` są obowiązkowe - bez nich plik nie jest fragmentem.
Nazwa fragmentu to `<nazwa warstwy>/<ścieżka w conf/fragments bez .conf>`, czyli `meta-kurs/kurs`.

    cd ~/yocto-kurs/meta-kurs
    git add conf/fragments/kurs.conf
    git commit -m "fragment kurs: konfiguracja z local.conf"

## Warstwa i fragment w kurs.conf.json

Otwórz na hoście `~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json` i wprowadź trzy zmiany:

1. W `sources` dopisz naszą warstwę:

        "meta-kurs": {
            "local": {
                "path": "/workdir/meta-kurs"
            }
        }

   `local` zamiast `git-remote`: `bitbake-setup` niczego nie pobiera, tylko tworzy w `layers/` dowiązanie
   do katalogu. Ścieżka musi być bezwzględna i widoczna tam, gdzie działa `bitbake-setup`, czyli w kontenerze.

2. W `bb-layers` dopisz `"meta-kurs"`.

3. W `oe-fragments` dopisz `"meta-kurs/kurs"`.

Zastosuj zmiany i odpowiedz `y`:

    # kontener
    bitbake-setup status # powinny się pokazać nowe zmiany
    bitbake-setup update

Sprawdź:

    # kontener
    ls -l /workdir/bitbake-builds/rpi4/layers
    bitbake-layers show-layers
    cat conf/local.conf
    cat conf/toolcfg.conf

`local.conf` znowu jest pusty - ale tym razem nic nie przywracasz, ustawienia są w warstwie. Od teraz `local.conf`
służy tylko do szybkich eksperymentów.

Zbuduj przykładową receptę, którą `create-layer` wygenerował w nowej warstwie:

    # kontener
    bitbake example

## Priorytety

`bitbake-layers show-layers` pokazuje priorytet każdej warstwy (`BBFILE_PRIORITY` z `layer.conf`).
Priorytet rozstrzyga, gdy ta sama recepta jest w kilku warstwach, i ustala kolejność nakładania `.bbappend`
(ćwiczenie 5).

**Q: Warstwa A (priorytet 5) ma `foo_2.0.bb`, warstwa B (priorytet 9) - `foo_1.0.bb`. Którą wersję zbuduje bitbake?**

## Zadanie

Sprawdź, czy obraz ma te same ustawienia co przed przeniesieniem do warstwy i skąd teraz pochodzą:

    # kontener
    bitbake-getvar -r core-image-base IMAGE_FEATURES
    bitbake-getvar -r core-image-base IMAGE_INSTALL

Źródła:
- [Yocto Dev Manual - Creating Your Own Layer](https://docs.yoctoproject.org/wrynose/dev-manual/layers.html#creating-your-own-layer)
- [Yocto Dev Manual - Creating New Configuration Fragments](https://docs.yoctoproject.org/wrynose/dev-manual/creating-fragments.html)
- [BitBake manual - Configuration Template Files Reference](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-environment-setup.html#configuration-template-files-reference)
  (`sources` -> `local`)
- [Yocto Reference - BBFILE_PRIORITY](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-BBFILE_PRIORITY)
