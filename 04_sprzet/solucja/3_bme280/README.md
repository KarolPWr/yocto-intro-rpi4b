# Rozwiązanie: czujnik BME280 po I2C

Pliki warstwy zmienione w ćwiczeniu:

    meta-kurs/
    ├── conf/fragments/kurs.conf
    └── recipes-bsp/bootfiles/rpi-config_git.bbappend

Zmiany:

    # kurs.conf
    ENABLE_I2C = "1"
    IMAGE_INSTALL:append = " i2c-tools"
    RPI_KERNEL_DEVICETREE_OVERLAYS:append = " overlays/i2c-sensor.dtbo"

    # rpi-config_git.bbappend
    RPI_EXTRA_CONFIG:append = "\ndtoverlay=kurs-led\ndtoverlay=i2c-sensor,bme280"

## Odpowiedzi

**config.txt po `ENABLE_I2C`:**

    # Enable I2C
    dtparam=i2c1=on
    dtparam=i2c_arm=on

`dtparam` zmienia parametr głównego device tree - tu włącza (`status = "okay"`) kontroler I2C na pinach GPIO2/GPIO3.
To szyna `i2c_arm`, w Linuxie `i2c-1` - na niej domyślnie działa też overlay `i2c-sensor`. `ENABLE_I2C` dokłada
też do obrazu moduł `i2c-dev`, ale go nie ładuje - stąd ręczny `modprobe i2c-dev`.

Źródła: meta-raspberrypi wrynose `recipes-bsp/bootfiles/rpi-config_git.bb`, `conf/machine/include/rpi-base.inc`
(`MACHINE_ESSENTIAL_EXTRA_RRECOMMENDS`).

**Adres czujnika:** zwykle `0x76`. Moduły BME280 mają adres `0x76` albo `0x77` - zależy od stanu pinu SDO na module
(do GND - `0x76`, do VCC - `0x77`).

**Linia z README:**

    dtoverlay=i2c-sensor,bme280

README overlayów: parametr `bme280` wybiera czujnik, `Valid addresses 0x76-0x77, default 0x76`. Jeśli `i2cdetect`
pokazał `77`, dopisz parametr `addr`:

    dtoverlay=i2c-sensor,bme280,addr=0x77

W overlayu (`i2c-sensor-common.dtsi`) jest węzeł:

    bme280: bme280@76 {
        compatible = "bosch,bme280";
        reg = <0x76>;
        status = "okay";
    };

Parametr `bme280` włącza fragment z tym węzłem, `addr` nadpisuje `reg`. `compatible` pasuje do aliasu modułu
(`modinfo bmp280_i2c` pokazuje `alias: of:N*T*Cbosch,bme280`) - udev sam ładuje `bmp280_i2c`.

**Jednostki (IIO):**
- `in_temp_input` - tysięczne części stopnia Celsjusza (`23450` = 23,45 °C),
- `in_pressure_input` - kilopaskale (`100.712...` = 1007 hPa),
- `in_humidityrelative_input` - tysięczne części procenta (`41230` = 41,23 %).

**`i2c-sensor.dtbo` w `IMAGE_BOOT_FILES`:** domyślne `IMAGE_BOOT_FILES` z `rpi-base.inc` woła funkcję
`make_dtb_boot_files`. Ta przechodzi po `KERNEL_DEVICETREE` (= `RPI_KERNEL_DEVICETREE` +
`RPI_KERNEL_DEVICETREE_OVERLAYS`) i z każdego `overlays/X.dtbo` robi wpis `X.dtbo;overlays/X.dtbo`. Overlaye
z kernela trafiają więc na partycję boot same. Nasz `kurs-led.dtbo` buduje osobna recepta, nie ma go
w `KERNEL_DEVICETREE` - dlatego dla niego dopisywaliśmy `IMAGE_BOOT_FILES` ręcznie.

Źródła: raspberrypi/linux rpi-6.18.y `arch/arm/boot/dts/overlays/README` (sekcja `i2c-sensor`),
`arch/arm/boot/dts/overlays/i2c-sensor-common.dtsi`, `drivers/iio/pressure/bmp280-i2c.c`,
`Documentation/ABI/testing/sysfs-bus-iio`; meta-raspberrypi wrynose `conf/machine/include/rpi-base.inc`;
openembedded-core wrynose `meta/classes-recipe/kernel-devicetree.bbclass` (`KERNEL_DEVICETREE` w `do_compile`).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/04_sprzet/solucja/3_bme280/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs.conf: BME280 na I2C (overlay i2c-sensor)"

W kontenerze:

    # kontener
    bitbake core-image-base

Wgranie na kartę - jak w ćwiczeniu 5 z dnia 1.
