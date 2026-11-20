# Operatory przypisania

Ta sama zmienna może być ustawiona w wielu miejscach: w `local.conf`, w konfiguracji distro, w klasie, w recepcie.
To, które przypisanie wygra, zależy od operatora i od kolejności parsowania.
W tym ćwiczeniu nic nie budujemy - wystarczy `bitbake-getvar`.

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Ściąga

| Operator | Co robi |
|---|---|
| `A = "x"` | przypisanie; `${...}` w wartości rozwijane dopiero przy użyciu zmiennej |
| `A := "x"` | przypisanie; `${...}` rozwijane od razu |
| `A ?= "x"` | przypisz, jeśli `A` w tym miejscu parsowania nie ma jeszcze wartości |
| `A ??= "x"` | słaba wartość domyślna - użyta, jeśli do końca parsowania nikt nie przypisze `A` inaczej |
| `A += "x"` / `A =+ "x"` | dopisz na końcu / na początku, ze spacją |
| `A .= "x"` / `A =. "x"` | dopisz na końcu / na początku, bez spacji |
| `A:append = "x"` / `A:prepend = "x"` | dopisz na końcu / na początku, bez spacji, po zakończeniu parsowania |

Źródło: [BitBake manual - Basic Syntax](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-metadata.html#basic-syntax)

## Przewidź, potem sprawdź

Dopisz na końcu `~/yocto-kurs/bitbake-builds/rpi4/build/conf/local.conf` (na hoście):

    # ćwiczenie 8 - operatory (po ćwiczeniu usuń)
    KURS_A = "pierwsze"
    KURS_A ?= "drugie"

    KURS_B ?= "pierwsze"
    KURS_B ?= "drugie"

    KURS_C ??= "pierwsze"
    KURS_C ??= "drugie"

    KURS_D ??= "slabe"
    KURS_D ?= "domyslne"

    KURS_E ??= "x"
    KURS_E += "y"

    KURS_F ??= "x"
    KURS_F:append = "y"

    KURS_G = "a"
    KURS_G += "b"
    KURS_G .= "c"
    KURS_G:append = " d"

    KURS_H = "start"
    KURS_H1 = "${KURS_H}"
    KURS_H2 := "${KURS_H}"
    KURS_H = "koniec"

    KURS_I = "jeden"
    KURS_I:append = " dwa"
    KURS_I = "trzy"

**Zanim cokolwiek uruchomisz, zapisz przy każdej zmiennej, jakiej wartości się spodziewasz.**

Sprawdź (nawiasy kwadratowe pokazują spacje na początku i na końcu wartości):

    # kontener
    for v in A B C D E F G H1 H2 I; do
        echo "KURS_$v = [$(bitbake-getvar --value KURS_$v)]"
    done

Każde wywołanie `bitbake-getvar` parsuje konfigurację, więc pętla chwilę potrwa.

Przy zmiennych, przy których się pomyliłeś, zobacz historię przypisań, np.:

    # kontener
    bitbake-getvar KURS_I

## Sprzątanie

Usuń z `local.conf` blok `KURS_*`.
