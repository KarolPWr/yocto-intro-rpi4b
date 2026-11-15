# Rozwiązanie: eksploracja systemu

## Odpowiedzi

**Co jest potrzebne na targecie do SSH:**
- serwer SSH w obrazie - u nas `ssh-server-dropbear` w `EXTRA_IMAGE_FEATURES` (ćwiczenie 4),
  który dodaje paczkę `packagegroup-core-ssh-dropbear` (`meta/classes-recipe/core-image.bbclass`),
- sieć - płytka musi mieć adres IP (u nas DHCP),
- zgoda na logowanie: fragment `root-login-with-empty-password` - bez niego dropbear nie wpuści roota
  ani konta bez hasła (`ssh_allow_root_login` / `ssh_allow_empty_password` w
  `meta/classes-recipe/rootfs-postcommands.bbclass`).

## Zadanie

1. Skrypt: [sys_monitor.sh](sys_monitor.sh)

   - temperatura: `/sys/class/thermal/thermal_zone0/temp` w milistopniach C (`type` = `cpu-thermal`),
   - taktowanie: `/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq` w kHz,
   - uptime: `/proc/uptime` - pierwsza liczba to sekundy od startu.

   Taktowanie zmienia się w czasie - steruje nim cpufreq governor
   (`cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor`, u nas `ondemand`). Żeby to zobaczyć,
   obciąż procesor w drugiej sesji SSH: `yes > /dev/null`.

   Data w logach może być dziwna zaraz po starcie - Raspberry Pi 4 nie ma zegara RTC, czas ustawia się dopiero
   z sieci.

2. Po restarcie plik z logami **zniknął**. W obrazie Yocto `/var/log` to link do `/var/volatile/log`,
   a `/var/volatile` to tmpfs (w RAM):

       ls -l /var/log
       mount | grep volatile

   To domyślne ustawienie w openembedded-core (`FILESYSTEM_PERMS_TABLES` zawiera `files/fs-perms-volatile-log.txt`) -
   logi nie zużywają karty SD. Wyłącza się je w konfiguracji distro, usuwając ten plik z `FILESYSTEM_PERMS_TABLES`.

   Źródło: [Yocto Reference - FILESYSTEM_PERMS_TABLES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-FILESYSTEM_PERMS_TABLES)

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Ćwiczenie nie zmienia obrazu ani konfiguracji buildu. Wystarczy działająca płytka z ćwiczenia 5.
