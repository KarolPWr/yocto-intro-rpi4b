# Aplikacja z serwisem systemd

Czujnik działa, temperaturę czytamy z `/sys`. Teraz program, który startuje razem z systemem i sam zapisuje
pomiary do pliku.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Zadanie

W warstwie `~/yocto-kurs/meta-kurs` napisz receptę `bme280-log` ze skryptem i serwisem systemd.

Skrypt `/usr/bin/bme280-log`:
- co 10 sekund dopisuje do pliku `/var/log/bme280.log` linię z datą i temperaturą w °C, np.
  `2026-11-25 10:15:00 23.45`,
- urządzenie IIO szuka po nazwie `bme280` - numer w `iio:deviceN` może się zmienić,
- czeka, aż urządzenie się pojawi - sterownik może się załadować później niż skrypt.

Serwis `bme280-log.service`:
- uruchamia skrypt przy starcie systemu,
- uruchamia go ponownie, gdy skrypt się zakończy z błędem.

Idź małymi krokami i sprawdzaj każdy z nich:
1. **Skrypt** - przetestuj ręcznie na płytce: `scp` do `/tmp` i uruchom.
2. **Recepta bez serwisu** - zbuduj samą receptę (`bitbake bme280-log`) i sprawdź, co jest w paczce
   (`oe-pkgdata-util`, ćwiczenie 2 z dnia 2).
3. **Serwis** - dodaj plik `.service` do recepty, zbuduj ją jeszcze raz i znów sprawdź paczkę.
4. **Obraz** - dopisz `bme280-log` do naszych programów we fragmencie `kurs.conf`, zbuduj obraz i wgraj go na kartę
   (razem ze zmianami z ćwiczenia 3).

Możesz korzystać z dowolnych narzędzi, np. `devtool`.

Wzór recepty z serwisem: `consolation_0.0.10.bb` w meta-oe:

    # kontener
    cat /workdir/bitbake-builds/rpi4/layers/meta-openembedded/meta-oe/recipes-support/consolation/consolation_0.0.10.bb

<details>
<summary>Podpowiedź - serwis w recepcie</summary>

- `inherit systemd`,
- `SYSTEMD_SERVICE:${PN} = "<nazwa>.service"` - klasa włączy serwis przy starcie (domyślnie
  `SYSTEMD_AUTO_ENABLE = "enable"`),
- plik `.service` instalujemy w `do_install` do `${D}${systemd_system_unitdir}`.

</details>

## Test

Na płytce:

    systemctl status bme280-log
    tail -f /var/log/bme280.log

Dotknij czujnika palcem - temperatura w logu powinna wzrosnąć. Zatrzymaj skrypt „na twardo” i zobacz, co zrobi
systemd:

    systemctl kill -s KILL bme280-log
    systemctl status bme280-log

Logi serwisu (to, co skrypt wypisał na stdout/stderr):

    journalctl -u bme280-log

Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "bme280-log: log temperatury jako serwis systemd"

Źródła:
- [Yocto Dev Manual - Using systemd Exclusively](https://docs.yoctoproject.org/wrynose/dev-manual/init-manager.html#using-systemd-exclusively)
- [Yocto Reference - systemd](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-systemd),
  [SYSTEMD_SERVICE](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-SYSTEMD_SERVICE),
  [SYSTEMD_AUTO_ENABLE](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-SYSTEMD_AUTO_ENABLE)
- [systemd.service](https://www.freedesktop.org/software/systemd/man/latest/systemd.service.html)
