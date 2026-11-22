# Rozwiązanie: U-Boot - menuconfig i własny prompt

Pliki: [meta-kurs/recipes-bsp/u-boot/](meta-kurs/recipes-bsp/u-boot/)

    meta-kurs/recipes-bsp/u-boot/
    ├── files/
    │   └── prompt.cfg
    └── u-boot_%.bbappend

`prompt.cfg`:

    CONFIG_SYS_PROMPT="kurs> "

## Zadanie - własny prompt

Opcja jest w menu `Command line interface -> Shell prompt`. W `menuconfig`:
1. `/` - wyszukiwarka, wpisz `SYS_PROMPT`, Enter,
2. wciśnij numer wyniku przy `SYS_PROMPT` (`1`) - przeskoczysz do opcji `Shell prompt`,
3. Enter - wpisz prompt, np. `kurs> ` (spacja na końcu oddziela prompt od wpisywanej komendy), Enter,
4. `Save` (nazwa `.config`), potem `Exit` aż do wyjścia.

Źródła: U-Boot v2026.01 `cmd/Kconfig` (`SYS_PROMPT`), `configs/rpi_arm64_defconfig` (`CONFIG_SYS_PROMPT="U-Boot> "`).

## Odpowiedzi

**Fragment z meta-raspberrypi:** `maxsize.cfg` (`CONFIG_SYS_BOOTM_LEN` - maksymalny rozmiar kernela, który U-Boot
rozpakuje), dopisany dla `raspberrypi4` (nasza maszyna `raspberrypi4-64` ma ten override w `MACHINEOVERRIDES`).

**`bootdelay` a prompt:** `CONFIG_BOOTDELAY` trafia do domyślnego środowiska jako zmienna `bootdelay`
(`include/env_default.h`) - U-Boot czyta ją przy starcie, więc `setenv` ją nadpisuje. `CONFIG_SYS_PROMPT` to stały
napis wkompilowany w kod konsoli (`common/cli_hush.c`, `common/cli_simple.c`) - żadna zmienna go nie zastępuje.
Zmianę w trakcie działania umożliwia dopiero opcja `CONFIG_CMDLINE_PS_SUPPORT` (prompt ze zmiennej `PS1`) - w U-Boocie
dla RPi jest wyłączona.

Źródła: openembedded-core wrynose `meta/recipes-bsp/u-boot/u-boot-configure.inc` (`merge_config.sh` z `find_cfgs`),
`meta/classes-recipe/cml1.bbclass` (`do_menuconfig`, `do_diffconfig`); meta-raspberrypi wrynose
`recipes-bsp/u-boot/u-boot_%.bbappend`, `conf/machine/raspberrypi4-64.conf`; U-Boot v2026.01 `cmd/Kconfig`
(`CMDLINE_PS_SUPPORT`), `common/cli_hush.c` (`uboot_cli_readline`), `include/env_default.h`;
[U-Boot - Environment Variables](https://docs.u-boot.org/en/latest/usage/environment.html).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/03_uboot/solucja/2_menuconfig/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "u-boot: własny prompt"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
