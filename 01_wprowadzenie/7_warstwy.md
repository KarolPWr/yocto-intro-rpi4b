# Warstwy: meta-openembedded i serwer Flask

Chcemy postawić na płytce prosty serwer WWW w Pythonie, we frameworku Flask.
Po drodze dodamy do projektu nową warstwę - tak, jak robi się to w bitbake-setup: przez plik JSON.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

Sprawdź, jakie warstwy są teraz w projekcie:

    # kontener
    bitbake-layers show-layers

## Dodanie paczki do obrazu

Dopisz na końcu `~/yocto-kurs/bitbake-builds/rpi4/build/conf/local.conf` (na hoście):

    # serwer WWW we Flasku
    IMAGE_INSTALL:append = " python3-flask"

Zbuduj obraz:

    # kontener
    bitbake core-image-base

Build kończy się od razu błędem:

    ERROR: Nothing RPROVIDES 'python3-flask' (but .../core-image-base.bb RDEPENDS on or otherwise requires it)

**Q: Co oznacza ten błąd? Jak go naprawić?**

## Gdzie jest Flask?

Recepturę z interesującą nas paczką znajdziesz w OpenEmbedded Layer Index. Wpisz `python3-flask` w wyszukiwarkę
receptur dla gałęzi wrynose:

https://layers.openembedded.org/layerindex/branch/wrynose/recipes/

Zapisz, w której warstwie jest receptura i w jakim repozytorium leży ta warstwa.

## Nowa warstwa w kurs.conf.json

Warstwy projektu opisuje nasz JSON. Otwórz na hoście `~/yocto-kurs/yocto-intro-rpi4b/kurs.conf.json`
i wprowadź trzy zmiany:

1. W `sources` dopisz repozytorium meta-openembedded - przypięte do tego samego commita, z którego zbudowany jest cache:

        "meta-openembedded": {
            "git-remote": {
                "uri": "https://git.openembedded.org/meta-openembedded",
                "branch": "wrynose",
                "rev": "c0fcd7cca3760ef24e087ac4a31b8f357cb204da"
            }
        }

2. W `bb-layers` dopisz warstwę z Flaskiem: `"meta-openembedded/meta-python"`.

3. W `oe-fragments` dopisz fragment, który w ćwiczeniu 4 włączyłeś ręcznie: `"core/yocto/root-login-with-empty-password"`.

Pilnuj przecinków - JSON nie wybacza.

Sprawdź, co widzi bitbake-setup:

    # kontener
    bitbake-setup status

`status` porównuje JSON z konfiguracją, z której powstał setup, i pokazuje różnice. Niczego nie zmienia.

## bitbake-setup update

    # kontener
    bitbake-setup update

`update` pobiera meta-openembedded, a potem kończy się błędem. W długim komunikacie znajdź linię:

    ERROR: Layer 'meta-python' depends on layer 'openembedded-layer', but this layer is not enabled in your configuration

**Q: Co oznacza ten błąd? Jak go naprawić?**

Napraw `kurs.conf.json` i uruchom `bitbake-setup update` jeszcze raz.

Tym razem `update` pokaże różnice w katalogu `conf/` i zapyta:

    Apply these changes to the current configuration? (y/N):

Przeczytaj różnice i odpowiedz `y`.

## Gdzie jest mój local.conf?

    # kontener
    cat conf/local.conf
    cat conf/toolcfg.conf
    ls -d conf-backup.*

`bitbake-setup update` wygenerował katalog `conf/` od nowa - z tego, co jest w JSON. Poprzednie wersje przeniósł do
`conf-backup.<data>`. Fragment root-login przetrwał, bo jest w JSON. `local.conf` jest pusty.

Każdy `update`, który zmienia `conf/`, robi nowy backup. Twój `local.conf` z dodatkami jest w **najstarszym** z nich.
Przywróć go (podmień `<data>`):

    # kontener
    cp conf-backup.<data>/local.conf conf/local.conf

Źródło: [BitBake manual - Setting up the environment with bitbake-setup](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-environment-setup.html)

**Q: Jak prawidłowo powinno wyglądać zapisywanie stałych zmian w konfiguracji?**

## Build

    # kontener
    bitbake-layers show-layers
    bitbake-layers show-recipes python3-flask
    bitbake core-image-base

**Zadanie:** zanim wgrasz obraz na kartę, sprawdź na hoście, czy Flask trafił do obrazu.

<details>
<summary>Podpowiedź</summary>

Lista paczek w obrazie jest w manifeście (ćwiczenie 6):

    grep flask ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/core-image-base-raspberrypi4-64.rootfs.manifest

</details>

Wgraj nowy obraz na kartę tak jak w ćwiczeniu 5 i uruchom płytkę. Pamiętaj o `ssh-keygen -R <ADRES_IP>`.

## Serwer Flask

Na hoście utwórz plik `hello.py`:

    from flask import Flask

    app = Flask(__name__)

    @app.route('/')
    def hello_world():
        return 'Hello, from Flask on Yocto!'

    if __name__ == '__main__':
        # 0.0.0.0 - serwer dostępny z sieci, nie tylko z płytki
        app.run(host='0.0.0.0', port=5000)

Skopiuj go na płytkę i uruchom:

    scp hello.py root@<ADRES_IP>:/root/
    ssh root@<ADRES_IP>
    python3 hello.py

Na hoście otwórz w przeglądarce:

    http://<ADRES_IP>:5000

## Zadanie [opcjonalne]

Rozbuduj serwer: dodaj stronę `/stats`, która zwróci temperaturę procesora i uptime płytki.
Dane weź z tych samych plików, co skrypt z ćwiczenia 6.
