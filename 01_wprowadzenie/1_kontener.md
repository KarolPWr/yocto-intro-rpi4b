# Praca z kontenerem

BTW - Jeżeli korzystasz z laptopa - ustaw Power Mode na opcję "Performance" 

Wszystko, co związane z bitbake, uruchamiamy w kontenerze [crops/poky](https://github.com/crops/poky-container).
Dzięki temu każdy ma identyczne środowisko do budowania, niezależnie od tego, co jest zainstalowane na hoście.

Obraz kontenera i katalog `~/yocto-kurs` masz już z [przygotowania](../00_przygotowanie/przygotowanie.md).

## Odpalanie kontenera

    docker run --rm --net=host -e HISTFILE=/workdir/.bash_history -it -v ~/yocto-kurs:/workdir crops/poky:ubuntu-22.04 --workdir=/workdir

Po chwili powinniśmy zobaczyć prompt:

    pokyuser@40e16a3f8d21:/workdir$

Co oznaczają opcje:
- `-v ~/yocto-kurs:/workdir` - katalog `~/yocto-kurs` z hosta jest widoczny w kontenerze jako `/workdir`,
- `--workdir=/workdir` - kontener startuje w tym katalogu i przejmuje **uid/gid jego właściciela**
  (dlatego pliki tworzone w kontenerze należą na hoście do Ciebie, a nie do roota),
- `--rm` - po wyjściu (`exit`) kontener jest usuwany,
- `-e HISTFILE=/workdir/.bash_history` - historia poleceń zapisuje się w `/workdir`, więc przetrwa restart kontenera,
- `--net=host` - kontener korzysta z sieci hosta (przyda się przy połączeniach z płytką).

Źródło: [crops/poky-container - README](https://github.com/crops/poky-container#readme)

## Co w kontenerze, a co na hoście

W kontenerze uruchamiamy polecenia związane z budowaniem: `bitbake`, `bitbake-setup`, `bitbake-layers`,
`devtool`, `recipetool` itp. Przy takich poleceniach w ćwiczeniach będzie dopisek `# kontener`.

Na hoście robimy resztę: edycja plików (dowolny edytor, np. VS Code), `git`, przeglądanie plików, wgrywanie
obrazu na kartę SD, konsola szeregowa, SSH do płytki.

Wygodnie jest mieć otwarte dwa terminale: jeden z kontenerem, drugi na hoście.

## Sprawdzenie

Utwórz plik w kontenerze:

    # kontener
    touch /workdir/test_z_kontenera

I sprawdź go na hoście:

    ls -l ~/yocto-kurs/test_z_kontenera

Usuń plik testowy:

    rm ~/yocto-kurs/test_z_kontenera

## Zadanie: skrypt startowy

Komenda do odpalenia kontenera jest długa, a będziemy jej używać kilka razy dziennie.

Napisz skrypt `~/start_yocto_env.sh`, który uruchamia kontener z powyższymi opcjami. Sprawdź, że działa:

    ~/start_yocto_env.sh

<details>
<summary>Podpowiedź</summary>

Skrypt można utworzyć jednym poleceniem:

```bash
cat << 'EOF' > ~/start_yocto_env.sh
#!/bin/bash
# tu komenda docker run
EOF

chmod +x ~/start_yocto_env.sh
```

</details>
