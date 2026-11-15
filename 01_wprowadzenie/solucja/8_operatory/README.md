# Rozwiązanie: operatory przypisania

## Przewidź, potem sprawdź

| Zmienna | Wartość | Dlaczego |
|---|---|---|
| `KURS_A` | `[pierwsze]` | `?=` nie przypisuje, bo `KURS_A` ma już wartość |
| `KURS_B` | `[pierwsze]` | drugie `?=` widzi wartość z pierwszego |
| `KURS_C` | `[drugie]` | każde `??=` zastępuje poprzednią słabą wartość - liczy się ostatnie |
| `KURS_D` | `[domyslne]` | słaba wartość z `??=` to nie „przypisana wartość” - `?=` przypisuje |
| `KURS_E` | `[ y]` | `+=` nie widzi słabej wartości: dopisuje do pustej zmiennej, ze spacją, a `??=` już się nie stosuje |
| `KURS_F` | `[xy]` | `:append` działa po parsowaniu, kiedy słaba wartość jest już podstawiona; bez spacji |
| `KURS_G` | `[a bc d]` | `+=` dodaje spację, `.=` nie; `:append` też nie - spacja jest w `" d"` |
| `KURS_H1` | `[koniec]` | `=` rozwija `${KURS_H}` dopiero przy odczycie, czyli po całym parsowaniu |
| `KURS_H2` | `[start]` | `:=` rozwija `${KURS_H}` w miejscu przypisania |
| `KURS_I` | `[trzy dwa]` | `:append` jest zapamiętany i stosowany na końcu, więc przeżywa późniejsze `=` |

Historia `KURS_I` (ścieżki skrócone):

    # $KURS_I [3 operations]
    #   set .../conf/local.conf:...
    #     "jeden"
    #   :append .../conf/local.conf:...
    #     " dwa"
    #   set .../conf/local.conf:...
    #     "trzy"
    # pre-expansion value:
    #   "trzy dwa"
    KURS_I="trzy dwa"

Źródło: [BitBake manual - Basic Syntax](https://docs.yoctoproject.org/bitbake/2.18/bitbake-user-manual/bitbake-user-manual-metadata.html#basic-syntax)
(sekcje „Setting a weak default value (??=)”, „Immediate variable expansion (:=)”,
„Appending and Prepending (Override Style Syntax)”).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

`local.conf` taki sam jak po ćwiczeniu 7 (bez bloku `KURS_*`) - zobacz `../7_warstwy/local.conf.dodatki`.
