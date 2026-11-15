# Rozwiązanie: cache

## Odpowiedzi

**`=` w `site.conf` i `local.conf`:** wygrywa `local.conf` - jest wczytywany później i nadpisuje wartość.

**Zadanie:**

1. Pliki kernela:

        # kontener
        find /workdir/cache/sstate-cache -name 'sstate:linux-raspberrypi:*'

   Plików jest kilka - każdy to wynik innego taska tej samej receptury (nazwa taska jest na końcu nazwy pliku).

2. Bez `build/tmp`: bitbake odtworzy wyniki z sstate - build potrwa minuty, a nie godziny.
   Bez `sstate-cache`: wszystko kompiluje się od zera (godziny). Źródła nadal są w `downloads`, więc przynajmniej
   nie pobieramy ich z internetu.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Skopiuj [site.conf](site.conf) do `~/yocto-kurs/bitbake-builds/site.conf`.
