# Rozwiązanie: bitbake-setup

## Odpowiedzi

**Hash w `rev`:** gałąź się zmienia - ktoś, kto zrobi `init` za tydzień, dostałby inne warstwy niż reszta grupy,
a gotowy sstate-cache przestałby pasować. Hash commita daje zawsze dokładnie ten sam kod.

**Zmiana maszyny:** `bitbake-config-build enable-fragment machine/<nazwa_maszyny>`

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cd ~/yocto-kurs
    git clone https://github.com/KarolPWr/yocto-intro-rpi4b.git

W kontenerze:

    # kontener
    cd /workdir
    git clone -b 2.18 https://git.openembedded.org/bitbake
    ./bitbake/bin/bitbake-setup init --non-interactive /workdir/yocto-intro-rpi4b/kurs.conf.json
    source /workdir/bitbake-builds/rpi4/build/init-build-env
