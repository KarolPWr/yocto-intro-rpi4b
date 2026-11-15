#!/bin/sh
# co 20 s dopisuje do pliku temperaturę CPU, taktowanie i uptime
# kopiowanie z hosta: scp sys_monitor.sh root@<ADRES_IP>:/tmp/
# uruchomienie na płytce: sh /tmp/sys_monitor.sh

LOGFILE="/var/log/sys_monitor.log"
INTERVAL=20

echo "logi: $LOGFILE, zatrzymanie: Ctrl+C"

while true; do
    timestamp=$(date "+%Y-%m-%d %H:%M:%S")

    # temperatura w milistopniach C
    temp_raw=$(cat /sys/class/thermal/thermal_zone0/temp)
    temp_c=$(awk "BEGIN {printf \"%.1f\", $temp_raw / 1000}")

    # taktowanie cpu0 w kHz
    freq_khz=$(cat /sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq)
    freq_mhz=$((freq_khz / 1000))

    # pierwsza liczba w /proc/uptime to sekundy od startu systemu
    uptime_s=$(cut -d. -f1 /proc/uptime)

    entry="[$timestamp] temp: ${temp_c} C | freq: ${freq_mhz} MHz | uptime: ${uptime_s} s"
    echo "$entry" >> "$LOGFILE"
    echo "$entry"

    sleep "$INTERVAL"
done
