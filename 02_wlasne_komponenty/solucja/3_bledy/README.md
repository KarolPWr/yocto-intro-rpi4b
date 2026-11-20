# Rozwiązanie: typowe błędy i debugowanie recept

## Odpowiedzi

**Gdzie bitbake szukał `helo.c`:** w katalogach z `FILESPATH`. Bazowe katalogi to `hello-1.0/`, `hello/` i `files/`
obok recepty. Do każdego z nich bitbake dokleja podkatalogi override'ów: najpierw wszystkie bazowe katalogi
z najbardziej szczegółowym (`.../poky-altcfg`), potem z następnym (`.../poky`, `.../raspberrypi4-64`, ...,
`.../aarch64`), a na końcu same bazowe katalogi. Dzięki temu ta sama recepta może mieć różne pliki
dla różnych maszyn i distro.

    # kontener
    bitbake-getvar -r hello --value FILESPATH | tr ':' '\n'

Źródło: [Yocto Reference - FILESPATH](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESPATH)

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Recepta `hello` bez błędów - jak w rozwiązaniu ćwiczenia 2 (`../2_hello/`).
