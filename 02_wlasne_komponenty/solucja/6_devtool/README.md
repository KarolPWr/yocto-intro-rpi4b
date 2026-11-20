# Rozwiązanie: devtool - zmiana w cudzym kodzie

Recepta po `devtool finish` i poprawce: [meta-kurs/recipes-kurs/nudoku/](meta-kurs/recipes-kurs/nudoku/)

    meta-kurs/recipes-kurs/nudoku/
    ├── nudoku/
    │   └── 0001-nudoku-add-name-to-help-text-h.patch
    └── nudoku_8.0.1.bb

## Co zrobił devtool finish

- z commitów w `workspace/sources/nudoku` zrobił patch (`git format-patch`) i zapisał go obok recepty,
  w katalogu `nudoku/` (`${BPN}`),
- dopisał patch do `SRC_URI` w recepcie,
- usunął nudoku z `workspace` - bitbake znowu buduje ze źródeł z `SRC_URI` (git, `SRCREV`) + patch.

Nazwa patcha pochodzi z pierwszej linii opisu commita - dlatego warto pisać go starannie.

## `po/Makevars` w `git status`

Przy `devtool modify` bitbake buduje prosto z `workspace/sources/nudoku`. `do_configure` klasy `autotools` (z gettextem)
dokleja do każdego `po/Makevars` w źródłach szablon `Makevars.template.minimal` - plik w repozytorium się zmienia.
To zmiana z budowania, nie nasza, więc przed commitem cofamy ją (`git restore po/Makevars`). Bez tego `devtool finish`
kończy się błędem `Source tree is not clean: M po/Makevars`.

Źródła: openembedded-core wrynose `meta/classes-recipe/autotools.bbclass` (`autotools_do_configure`, `Makevars`),
`scripts/lib/devtool/standard.py` (`finish`, sprawdzenie `git status --porcelain`).

## Odpowiedzi

**Missing Upstream-Status in patch:** każdy patch w recepcie musi mieć w nagłówku linię `Upstream-Status`:
czy zmiana została (albo zostanie) wysłana do autorów programu. W wrynose brak tej linii to błąd QA, a nie
ostrzeżenie: test `patch-status` jest w `ERROR_QA` dla wszystkich warstw. Nasza zmiana jest tylko na kurs, więc:

    Subject: [PATCH] nudoku: add name to help text (-h)

    Upstream-Status: Inappropriate [kurs Yocto]
    ---
     src/main.c | 1 +
     ...

Najprościej dopisać tę linię do opisu commita jeszcze przed `devtool finish` - wtedy trafi do patcha sama.

Inne wartości: `Pending` (jeszcze nie wysłane), `Submitted [gdzie]`, `Backport [link do commita]`,
`Denied`, `Inactive-Upstream`.

Źródła: [Yocto Contributor Guide - Patch Upstream Status](https://docs.yoctoproject.org/wrynose/contributor-guide/recipe-style-guide.html#patch-upstream-status),
openembedded-core wrynose `meta/lib/oe/qa.py` (`check_upstream_status`), `meta/classes-global/insane.bbclass`
(`do_qa_patch`, `ERROR_QA`).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście (podmień w patchu imię na swoje, jeśli chcesz):

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/02_wlasne_komponenty/solucja/6_devtool/meta-kurs/recipes-kurs ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "nudoku: patch z imieniem w pomocy"

Jeśli `devtool modify nudoku` został bez `devtool finish`, usuń nudoku z `workspace`:

    # kontener
    devtool reset nudoku

Potem:

    # kontener
    bitbake nudoku
