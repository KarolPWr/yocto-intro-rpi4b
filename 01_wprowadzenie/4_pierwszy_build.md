# Pierwszy build

Budujemy obraz `core-image-base` dla Raspberry Pi 4 - system z konsolą, bez środowiska graficznego.
Zanim odpalimy build, dopasujemy konfigurację na dwa sposoby: przez fragmenty konfiguracji i przez `local.conf`.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Fragmenty konfiguracji

"Fragment" to mały plik `.conf` w warstwie, z nazwą i opisem, który włącza się jednym poleceniem.
Maszyna i distro z naszego JSON to właśnie fragmenty. Lista fragmentów - włączonych i dostępnych:

    # kontener
    bitbake-config-build list-fragments

Włączone fragmenty są zapisane w `conf/toolcfg.conf`:

    # kontener
    cat conf/toolcfg.conf

Źródło: [Yocto Reference - toolcfg.conf](https://docs.yoctoproject.org/wrynose/ref-manual/structure.html#structure-build-conf-toolcfg-conf),
[Creating Configuration Fragments](https://docs.yoctoproject.org/wrynose/dev-manual/creating-fragments.html)

### Logowanie na roota

W Wrynose obraz domyślnie **nie pozwala zalogować się na roota** - to bezpieczne ustawienie dla produktu, ale na kursie
chcemy się logować bez hasła przez UART i SSH. Służy do tego gotowy fragment z openembedded-core. Zobacz, co ustawia:

    # kontener
    bitbake-config-build show-fragment core/yocto/root-login-with-empty-password

Włącz go:

    # kontener
    bitbake-config-build enable-fragment core/yocto/root-login-with-empty-password
    cat conf/toolcfg.conf

**Takiego obrazu nigdy nie używa się w finalnym produkcie.**

## local.conf

`local.conf` to lokalne ustawienia buildu na Twoim komputerze. Otwórz na hoście
`~/yocto-kurs/bitbake-builds/rpi4/build/conf/local.conf` - `bitbake-setup` utworzył go prawie pustego.

Dopisz na końcu:

    # akceptacja licencji firmware WiFi/BT (linux-firmware-rpidistro)
    LICENSE_FLAGS_ACCEPTED = "synaptics-killswitch"

    # konsola szeregowa na pinach GPIO (UART)
    ENABLE_UART = "1"

    # serwer SSH
    EXTRA_IMAGE_FEATURES += "ssh-server-dropbear"

Co to jest:
- `LICENSE_FLAGS_ACCEPTED` - świadoma zgoda na licencję
  ([meta-raspberrypi - IP Compliance](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/ipcompliance.md)),
- `ENABLE_UART` - na RPi 4 UART na GPIO jest domyślnie wyłączony
  ([meta-raspberrypi - Enable UART](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/extra-build-config.md#enable-uart)),
- `EXTRA_IMAGE_FEATURES` - dodatkowe funkcje obrazu; `ssh-server-dropbear` to lekki serwer SSH
  ([Yocto Reference - Image Features](https://docs.yoctoproject.org/wrynose/ref-manual/features.html#ref-features-image)).

### Fragment czy local.conf?

Oba sposoby zmieniają konfigurację buildu. Różnica:
- `local.conf` - Twój plik, nigdzie nie jest wersjonowany. Szybki do eksperymentów, ale łatwo o nim zapomnieć
  i trudno go przekazać innym,
- fragment - leży w warstwie, ma nazwę i opis, można go wpisać do JSON z konfiguracją projektu
  i każdy w zespole włączy go tym samym poleceniem.

## Build

    # kontener
    time bitbake core-image-base

Na początku bitbake parsuje recepty, potem sprawdza, ile wyników tasków może wziąć z sstate-cache:

    Sstate summary: Wanted ... Local ... Mirrors ... Missed ... Current ... (..% match, ..% complete)

`Local` to wyniki znalezione w naszym cache, `Missed` - taski, które trzeba wykonać od zera.

**Q: Gdzie znajduje się wynikowy obraz? Jakie pliki obrazu powstały?**

## Zadanie

Sprawdź, czy nasze zmiany trafiły do konfiguracji obrazu:

    # kontener
    bitbake-getvar -r core-image-base IMAGE_FEATURES

1. Czy na liście są `ssh-server-dropbear` i funkcje z fragmentu root-login?
2. Z których plików pochodzą?
