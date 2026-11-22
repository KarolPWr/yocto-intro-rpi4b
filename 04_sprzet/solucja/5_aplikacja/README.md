# Rozwiązanie: aplikacja z serwisem systemd

Pliki warstwy po ćwiczeniu:

    meta-kurs/
    ├── conf/fragments/kurs.conf
    └── recipes-kurs/bme280-log/
        ├── files/
        │   ├── bme280-log.service
        │   └── bme280-log.sh
        └── bme280-log_1.0.bb

- Skrypt: [meta-kurs/recipes-kurs/bme280-log/files/bme280-log.sh](meta-kurs/recipes-kurs/bme280-log/files/bme280-log.sh)
- Serwis: [meta-kurs/recipes-kurs/bme280-log/files/bme280-log.service](meta-kurs/recipes-kurs/bme280-log/files/bme280-log.service)
- Recepta: [meta-kurs/recipes-kurs/bme280-log/bme280-log_1.0.bb](meta-kurs/recipes-kurs/bme280-log/bme280-log_1.0.bb)

We fragmencie `kurs.conf`:

    IMAGE_INSTALL:append = " hello nudoku kurs-mod bme280-log"

## Krok po kroku

**1. Skrypt na płytce** (z hosta):

    scp ~/yocto-kurs/meta-kurs/recipes-kurs/bme280-log/files/bme280-log.sh root@<ADRES_IP>:/tmp/

Na płytce: `sh /tmp/bme280-log.sh`, w drugim oknie `tail -f /var/log/bme280.log`, `Ctrl+C` kończy.

**2. i 3. Recepta i paczka:**

    # kontener
    bitbake bme280-log
    oe-pkgdata-util list-pkg-files bme280-log

Bez serwisu w paczce jest tylko `/usr/bin/bme280-log`. Z serwisem dochodzi
`/usr/lib/systemd/system/bme280-log.service`.

Klasa `systemd` dopisuje do paczki skrypty `postinst`/`prerm`, które przy instalacji włączają serwis
(`systemctl enable` - link w `/etc/systemd/system/multi-user.target.wants/`). Przy budowie obrazu `postinst`
wykonuje się już na hoście (`systemctl --root=<rootfs> enable`), więc serwis jest włączony od pierwszego startu.
Na płytce:

    systemctl is-enabled bme280-log
    ls -l /etc/systemd/system/multi-user.target.wants/ | grep bme280

Z `devtool`: `devtool add bme280-log <katalog ze skryptem>` tworzy receptę w `workspace` i pozwala na
`devtool deploy-target` bez flashowania (tylko dla recept w `workspace`). Na koniec `devtool finish bme280-log
/workdir/meta-kurs`.

## Odpowiedzi

**`systemctl kill -s KILL`:** skrypt zginął od sygnału - dla systemd to błąd, więc `Restart=on-failure` uruchamia
go ponownie (w `systemctl status` widać nowy PID). Po `systemctl stop` systemd go nie uruchomi - to zatrzymanie
na żądanie.

**Log po restarcie jest pusty (albo ma tylko nowe linie):** w obrazach Yocto `/var/log` to link do
`/var/volatile/log`, a `/var/volatile` to `tmpfs` - system plików w RAM, czyszczony przy każdym starcie:

    ls -l /var/log
    mount | grep volatile

Dzięki temu logi nie zużywają karty SD. Żeby pomiary przetrwały restart, zapisuj je np. do `/var/lib/bme280/`
(w skrypcie `mkdir -p` przed pętlą) albo do katalogu domowego roota - `/root` (z systemd w poky
`ROOT_HOME = "/root"`).

Źródła: openembedded-core wrynose `meta/files/fs-perms-volatile-log.txt` (`${localstatedir}/log link volatile/log`),
`meta/conf/bitbake.conf` (`FILESYSTEM_PERMS_TABLES`), `meta/recipes-core/base-files/base-files/fstab`
(`tmpfs /var/volatile`), `meta/classes-recipe/systemd.bbclass` (`SYSTEMD_AUTO_ENABLE ??= "enable"`,
`systemd_postinst`), `meta/conf/distro/include/init-manager-systemd.inc` (`ROOT_HOME`); [systemd.service - Restart=](https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html#Restart=).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/04_sprzet/solucja/5_aplikacja/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "bme280-log: log temperatury jako serwis systemd"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
