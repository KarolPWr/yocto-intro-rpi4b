# Rozwiązanie: wariant z czujnikiem SHT35

Pliki warstwy - jak w rozwiązaniu ćwiczenia 4, inna jest tylko linia overlaya:

    meta-kurs/
    ├── conf/fragments/kurs.conf                       # bez zmian względem 4_bme280
    └── recipes-bsp/bootfiles/rpi-config_git.bbappend

    # rpi-config_git.bbappend
    RPI_EXTRA_CONFIG:append = "\ndtoverlay=kurs-led\ndtoverlay=i2c-sensor,sht3x"

## Odpowiedzi

**Adres czujnika:** `0x44` (pin `ADDR` do GND) albo `0x45` (pin `ADDR` do VCC) - nota katalogowa SHT3x-DIS,
tabela adresów I2C. Dla `0x45`:

    dtoverlay=i2c-sensor,sht3x,addr=0x45

README overlayów: parametr `sht3x` - `Valid addresses 0x44-0x45, default 0x44`. W `i2c-sensor-common.dtsi`:

    sht3x: sht3x@44 {
        compatible = "sensirion,sht3x";
        reg = <0x44>;
        status = "okay";
    };

`compatible` pasuje do aliasu modułu `sht3x` (`alias: of:N*T*Csensirion,sht3x`), udev ładuje go sam.
Moduł jest w obrazie, bo `bcm2711_defconfig` ma `CONFIG_SENSORS_SHT3x=m`, a meta-raspberrypi dokłada do obrazu
`kernel-modules` (`MACHINE_EXTRA_RRECOMMENDS` w `rpi-base.inc`).

**hwmon zamiast IIO:** SHT3x ma sterownik w `drivers/hwmon/`, więc odczyty są w `/sys/class/hwmon/hwmonN/`,
a nie w `/sys/bus/iio/devices/`. Plik `name` zawiera `sht3x`.

**Jednostki (hwmon):**
- `temp1_input` - tysięczne części stopnia Celsjusza (`23450` = 23,45 °C),
- `humidity1_input` - tysięczne części procenta (`41230` = 41,23 %).

Temperatura ma tę samą skalę co `in_temp_input` z BME280 - w skrypcie z ćwiczenia 5 dzielenie przez 1000 zostaje.

**Ćwiczenie 5 - funkcja szukająca czujnika:**

    # szukamy urządzenia hwmon po nazwie - numer hwmonN może się zmienić
    find_dev() {
        for dev in /sys/class/hwmon/hwmon*; do
            if [ "$(cat "$dev/name" 2>/dev/null)" = "sht3x" ]; then
                echo "$dev"
                return 0
            fi
        done
        return 1
    }

i odczyt `temp=$(cat "$DEV/temp1_input")`.

Źródła: raspberrypi/linux rpi-6.18.y `arch/arm/boot/dts/overlays/README` (sekcja `i2c-sensor`),
`arch/arm/boot/dts/overlays/i2c-sensor-common.dtsi`, `drivers/hwmon/sht3x.c`, `Documentation/hwmon/sht3x.rst`,
`Documentation/ABI/testing/sysfs-class-hwmon`, `arch/arm64/configs/bcm2711_defconfig`;
meta-raspberrypi wrynose `conf/machine/include/rpi-base.inc`, `recipes-kernel/linux/linux-raspberrypi.inc`
(`KBUILD_DEFCONFIG`); Sensirion, nota katalogowa SHT3x-DIS.

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/04_sprzet/solucja/4a_sht35/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs.conf: SHT35 na I2C (overlay i2c-sensor)"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
