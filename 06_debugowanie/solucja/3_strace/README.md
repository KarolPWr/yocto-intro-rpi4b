# Rozwiązanie: strace

Pliki warstwy po ćwiczeniu (z poprawką z Zadania):

    meta-kurs/
    ├── conf/fragments/debug.conf
    └── recipes-kurs/streaming-app/
        ├── files/
        │   ├── streaming-app.c
        │   └── streaming-app.conf
        └── streaming-app_1.0.bb

- Fragment: [meta-kurs/conf/fragments/debug.conf](meta-kurs/conf/fragments/debug.conf)
- Recepta: [meta-kurs/recipes-kurs/streaming-app/streaming-app_1.0.bb](meta-kurs/recipes-kurs/streaming-app/streaming-app_1.0.bb)
- Konfiguracja tmpfiles: [meta-kurs/recipes-kurs/streaming-app/files/streaming-app.conf](meta-kurs/recipes-kurs/streaming-app/files/streaming-app.conf)

## Pierwsze kroki

**`-f`:** bez `-f` widać tylko `execve("/bin/sh", ...)` - `cat` działa w procesie potomnym, którego `strace` nie
śledzi. Z `-f` dochodzi `[pid NNN] execve(".../cat", ...) = 0` - powłoka może najpierw spróbować innych katalogów
z `PATH` (linie z `ENOENT`). W obrazie z busyboxem `cat` to link do busyboxa. `echo` jest wbudowane w powłokę,
więc nie uruchamia nowego programu.

## Zadanie

**1. Przyczyna:**

    strace -e trace=openat streaming-app

W śladzie, po otwarciu bibliotek przez loader:

    openat(AT_FDCWD, "/tmp/.vendor_license", O_RDONLY) = -1 ENOENT (No such file or directory)

Program szuka pliku licencji `/tmp/.vendor_license`, a ten nie istnieje. Ścieżka jest w programie zaszyfrowana,
więc nie znajdzie jej ani `grep`, ani `strings` - widać ją dopiero w syscallu. Naprawa na płytce:

    touch /tmp/.vendor_license
    streaming-app

Program sprawdza tylko, czy plik da się otworzyć - wystarczy pusty.

**2. Po restarcie:**

    mount | grep /tmp

`/tmp` to `tmpfs` - system plików w RAM, czyszczony przy każdym starcie. W openembedded-core to ustawienie
domyślne: `FILESYSTEM_PERMS_TABLES` zawiera `files/fs-perms-volatile-tmp.txt`, a recepta systemd zostawia wtedy
w obrazie jednostkę `tmp.mount`. Pliku w `/tmp` nie da się więc też po prostu dodać do obrazu - przy starcie
przykryje go pusty `tmpfs`.

Źródła: openembedded-core wrynose `meta/conf/bitbake.conf` (`FILESYSTEM_PERMS_TABLES`),
`meta/recipes-core/systemd/systemd_*.bb` (`tmp.mount`);
[Yocto Reference - FILESYSTEM_PERMS_TABLES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESYSTEM_PERMS_TABLES).

**3. Poprawka w warstwie:** `files/streaming-app.conf`:

    # licencja streaming-app - /tmp to tmpfs, plik trzeba tworzyć przy każdym starcie
    f /tmp/.vendor_license 0644 root root -

Typ `f` tworzy plik, jeśli go nie ma. Serwis `systemd-tmpfiles-setup.service` czyta katalogi `tmpfiles.d` przy każdym
starcie. Recepta instaluje plik do `/etc/tmpfiles.d/` - ten katalog jest w domyślnym `FILES:${PN}`
(`${sysconfdir}`), więc nie trzeba zmieniać `FILES`. Katalog `/usr/lib/tmpfiles.d/` też by zadziałał, ale wymaga
dopisania go do `FILES:${PN}`.

Po wgraniu obrazu i restarcie:

    ls -l /tmp/.vendor_license
    streaming-app

Źródła: [systemd - tmpfiles.d](https://www.freedesktop.org/software/systemd/man/latest/tmpfiles.d.html);
openembedded-core wrynose `meta/conf/bitbake.conf` (`FILES:${PN}`).

## Dodatkowo

**Programy przy każdym pomiarze:** skrypt `bme280-log` co 10 sekund uruchamia (`execve`) `cat` (odczyt
temperatury), `date`, `awk` (przeliczenie na °C) i `sleep`. `$(...)` tworzy proces potomny, dlatego potrzebne jest
`-f`. Bez niego `strace` pokaże tylko syscalle samej powłoki: tworzenie procesów (`clone`), czekanie na nie
(`wait4`) i zapis do logu.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/06_debugowanie/solucja/3_strace/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "streaming-app: program do ćwiczenia ze strace, fragment debug"

Jeśli robiłeś ćwiczenie 2, `cp` nadpisze `debug.conf` - zostanie w nim sam `strace` (bez gdb). Przywróć poprzednią
wersję (`git diff`, `git checkout conf/fragments/debug.conf`) i dopisz w niej `streaming-app`.

W kontenerze:

    # kontener
    bitbake-config-build enable-fragment meta-kurs/debug
    bitbake kurs-image

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1, plik `kurs-image-raspberrypi4-64.rootfs.wic.bz2`.
