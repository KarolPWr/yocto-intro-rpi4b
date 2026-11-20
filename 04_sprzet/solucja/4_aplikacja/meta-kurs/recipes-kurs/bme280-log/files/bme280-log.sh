#!/bin/sh
# co INTERVAL sekund dopisuje temperaturę z BME280 do pliku LOG

LOG=/var/log/bme280.log
INTERVAL=10

# szukamy urządzenia IIO po nazwie - numer iio:deviceN może się zmienić
find_dev() {
    for dev in /sys/bus/iio/devices/iio:device*; do
        if [ "$(cat "$dev/name" 2>/dev/null)" = "bme280" ]; then
            echo "$dev"
            return 0
        fi
    done
    return 1
}

# sterownik może się załadować później niż nasz serwis
until DEV=$(find_dev); do
    sleep 1
done
echo "bme280-log: czujnik w $DEV, log w $LOG"

while true; do
    # in_temp_input - tysięczne części stopnia Celsjusza
    temp=$(cat "$DEV/in_temp_input") || exit 1
    echo "$(date '+%Y-%m-%d %H:%M:%S') $(awk -v t="$temp" 'BEGIN { printf "%.2f", t / 1000 }')" >> "$LOG"
    sleep "$INTERVAL"
done
