# Wariant: czujnik SHT35 zamiast BME280

Dla osób, które mają czujnik Sensirion SHT35 (temperatura + wilgotność, bez ciśnienia). Robisz ćwiczenie 4
([4_bme280.md](4_bme280.md)) po kolei, z różnicami opisanymi niżej. Reszta (szyna I2C, `i2c-tools`, overlay
`i2c-sensor` w `RPI_KERNEL_DEVICETREE_OVERLAYS`, budowanie) - bez zmian.

## Podłączenie

Przy wyłączonym zasilaniu:

| SHT35 | Raspberry Pi |
|---|---|
| VIN / VCC | 3V3 (pin 1) |
| GND | GND (pin 9) |
| SCL | GPIO3 / SCL (pin 5) |
| SDA | GPIO2 / SDA (pin 3) |

Piny znajdziesz na schemacie złącza: [RPI_PINOUT.png](../RPI_PINOUT.png).

SHT35 działa też na 5 V, ale podłącz go do 3,3 V - rezystory podciągające na module podciągają SDA/SCL do jego
zasilania, a piny RPi przyjmują najwyżej 3,3 V. Pinów `ADDR` i `ALERT` (jeśli moduł je ma) nie podłączamy.

## Adres

Po `i2cdetect -y 1` czujnik odpowie pod `0x44` albo `0x45` - zależy od pinu `ADDR` (na module zwykle
podciągnięty rezystorem albo zworką).

**Q: Pod jakim adresem jest Twój czujnik?**

## Overlay czujnika

Sterownik SHT3x jest w kernelu RPi jako moduł, ale nie w podsystemie IIO, tylko hwmon (hardware monitoring):

    modinfo sht3x | grep -E "filename|alias"

W [README overlayów](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/arch/arm/boot/dts/overlays/README)
znajdź w sekcji `i2c-sensor` parametr `sht3x`. W ćwiczeniu 4 wszędzie, gdzie jest `bme280`, użyj `sht3x`:

    dtoverlay=i2c-sensor,sht3x

Domyślny adres to `0x44`. Jeśli `i2cdetect` pokazał `45`, dopisz na końcu linii `,addr=0x45`.

## Test

Na płytce, po restarcie z nałożonym overlayem:

    modprobe i2c-dev
    i2cdetect -y 1
    find /proc/device-tree/ -name "sht3x*"
    lsmod | grep sht3x
    ls -l /sys/bus/i2c/devices/1-0044/driver

Pod adresem czujnika jest `UU`, a urządzenie `1-0044` (u Ciebie może być `1-0045`) ma podpięty sterownik `sht3x`.

Odczyty są w `/sys/class/hwmon/`. Pierwsze `hwmonN` to zwykle czujnik temperatury procesora, więc znajdź
katalog o nazwie `sht3x`:

    grep . /sys/class/hwmon/hwmon*/name

W tym katalogu (niżej `hwmon2` - u Ciebie numer może być inny):

    cat /sys/class/hwmon/hwmon2/temp1_input
    cat /sys/class/hwmon/hwmon2/humidity1_input

Chuchnij na czujnik i odczytaj wilgotność jeszcze raz.

**Q: W jakich jednostkach są te wartości?**

## Do obrazu

Jak w ćwiczeniu 4, tylko linia w `rpi-config_git.bbappend` to `dtoverlay=i2c-sensor,sht3x` (ewentualnie
z `,addr=0x45`). Commit:

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs.conf: SHT35 na I2C (overlay i2c-sensor)"

## Ćwiczenie 5 z SHT35

Nazwy (`bme280-log`, `/var/log/bme280.log`) możesz zostawić - wtedy komendy z ćwiczenia 5 pasują bez zmian.
Zmienia się tylko to, skąd skrypt czyta temperaturę:
- urządzenie szukasz w `/sys/class/hwmon/hwmon*` po nazwie `sht3x` (zamiast `/sys/bus/iio/devices/iio:device*`
  i `bme280`),
- temperatura jest w pliku `temp1_input` (zamiast `in_temp_input`).

## Gdy czujnik nie działa

Jak w ćwiczeniu 4, z innymi nazwami:
- węzeł w device tree: `find /proc/device-tree/ -name "sht3x*"` - adres w nazwie (`sht3x@44` / `sht3x@45`) musi się
  zgadzać z `i2cdetect`,
- sterownik: `lsmod | grep sht3x`, `dmesg | grep -i -E "sht3x|i2c"`.

Źródła:
- [Raspberry Pi Linux - README overlayów](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/arch/arm/boot/dts/overlays/README)
  (sekcja `i2c-sensor`, parametr `sht3x`),
  [i2c-sensor-common.dtsi](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/arch/arm/boot/dts/overlays/i2c-sensor-common.dtsi)
- [Linux kernel - sterownik sht3x](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/Documentation/hwmon/sht3x.rst),
  [ABI sysfs-class-hwmon](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/Documentation/ABI/testing/sysfs-class-hwmon)
- [Sensirion - nota katalogowa SHT3x-DIS](https://sensirion.com/media/documents/213E6A3B/63A5A569/Datasheet_SHT3x_DIS.pdf)
