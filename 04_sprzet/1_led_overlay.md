# Device Tree overlay: dioda LED

Kernel nie wykryje sam diody podłączonej do GPIO - musi się o niej dowiedzieć z device tree. Nie zmieniamy głównego
device tree Raspberry Pi. Dopisujemy overlay: kawałek device tree, który firmware RPi nakłada na główne drzewo
przy starcie. Które overlaye nałożyć, firmware czyta z `config.txt` (linie `dtoverlay=`).

Ustaw środowisko:

    # kontener
    source /workdir/bitbake-builds/rpi4/build/init-build-env

## Podłączenie

Przy wyłączonym zasilaniu podłącz na płytce stykowej:

    GPIO23 (pin 16) -> rezystor 220 Ω -> dioda LED (dłuższa nóżka) -> krótsza nóżka -> GND (pin 14)

Piny znajdziesz na schemacie złącza: [RPI_PINOUT.png](../RPI_PINOUT.png).
`GPIO23` to numer linii GPIO w procesorze, a `pin 16` to fizyczny pin na złączu - to nie to samo.

## Overlay

Na hoście utwórz w warstwie plik `~/yocto-kurs/meta-kurs/recipes-bsp/kurs-overlays/files/kurs-led.dts`:

    /dts-v1/;
    /plugin/;

    / {
    	compatible = "brcm,bcm2835";

    	fragment@0 {
    		// konfiguracja pinu w kontrolerze GPIO
    		target = <&gpio>;
    		__overlay__ {
    			kurs_led_pin: kurs_led_pin {
    				brcm,pins = <XXX>;     // numer GPIO
    				brcm,function = <XXX>; // 0 = wejście, 1 = wyjście
    				brcm,pull = <XXX>;     // 0 = brak, 1 = pull-down, 2 = pull-up
    			};
    		};
    	};

    	fragment@1 {
    		// nowy węzeł w device tree root
    		target-path = "/";
    		__overlay__ {
    			kurs-leds {
    				compatible = "gpio-leds";
    				pinctrl-names = "default";
    				pinctrl-0 = <&kurs_led_pin>;
    				status = "XXX";

    				led {
    					label = "XXX";
    					gpios = <&gpio XXX 0>; // 0 = aktywny stan wysoki
    					linux,default-trigger = "XXX";
    				};
    			};
    		};
    	};
    };

**ZADANIE**: Uzupełnij pola `XXX`. Diodę nazwij `kurs`, i ustaw taki tryb, żeby dioda migała jak bicie serca. Skorzystaj z załączonej dokumentacji.

- `target = <&gpio>` - fragment modyfikuje istniejący węzeł z etykietą `gpio` (kontroler GPIO),
- `target-path = "/"` - fragment dodaje nowy węzeł w korzeniu drzewa,
- `compatible = "gpio-leds"` - po tym napisie kernel wybiera sterownik.

Dokumentacja:
[binding gpio-leds](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/Documentation/devicetree/bindings/leds/leds-gpio.yaml),
[overlaye Raspberry Pi](https://github.com/raspberrypi/linux/tree/rpi-6.18.y/arch/arm/boot/dts/overlays) -
podobny overlay to `gpio-led-overlay.dts`.

## Recepta

`~/yocto-kurs/meta-kurs/recipes-bsp/kurs-overlays/kurs-overlays_1.0.bb`:

    SUMMARY = "Overlaye Device Tree - kurs Yocto"
    LICENSE = "MIT"
    LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

    inherit devicetree

    SRC_URI = "file://kurs-led.dts"

    # tylko dla Raspberry Pi
    COMPATIBLE_MACHINE = "^rpi$"


Klasa `devicetree` kompiluje kompilatorem `dtc` każdy plik `.dts` z `SRC_URI` i kopiuje wynik do katalogu
`tmp/deploy/images/<maszyna>/devicetree/`.

Zbuduj:

    # kontener
    bitbake kurs-overlays
    ls /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/devicetree/


## Szybki test

Partycja boot karty SD jest w systemie płytki zamontowana w `/boot`. Overlay możesz sprawdzić bez nowego obrazu.

Skopiuj overlay na płytkę (z hosta):

    scp ~/yocto-kurs/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/devicetree/kurs-led.dtbo root@<ADRES_IP>:/boot/overlays/

Na płytce dopisz overlay do `config.txt` i zrestartuj:

    echo "dtoverlay=kurs-led" >> /boot/config.txt
    sync
    reboot

Dioda powinna migać. Steruj nią z userspace przez sysfs:

    ls /sys/class/leds/
    cat /sys/class/leds/kurs/trigger
    echo none > /sys/class/leds/kurs/trigger
    echo 1 > /sys/class/leds/kurs/brightness
    echo 0 > /sys/class/leds/kurs/brightness
    echo heartbeat > /sys/class/leds/kurs/trigger

Device tree, którego używa działający kernel, widać w `/proc/device-tree`:

    ls /proc/device-tree/
    cat /proc/device-tree/kurs-leds/led/label

Dioda nie świeci? Sprawdź kierunek diody, czy `/boot/overlays/kurs-led.dtbo` istnieje, ostatnie linie
`/boot/config.txt` i `dmesg | grep -i led`.

## Do obrazu

Żeby overlay działał po wgraniu obrazu, na partycji boot muszą być:
1. plik `overlays/kurs-led.dtbo`,
2. linia `dtoverlay=kurs-led` w `config.txt`.

**Plik.** Listę plików kopiowanych na partycję boot trzyma `IMAGE_BOOT_FILES` (widzieliśmy ją w ćwiczeniu z U-Bootem).
Dopisz na końcu fragmentu `~/yocto-kurs/meta-kurs/conf/fragments/kurs.conf`:

    # nasze overlaye DT na partycji boot (recepta kurs-overlays)
    IMAGE_BOOT_FILES:append = " devicetree/kurs-led.dtbo;overlays/kurs-led.dtbo"
    do_image_wic[depends] += "kurs-overlays:do_deploy"

`do_image_wic[depends]` - zanim powstanie obraz `.wic`, bitbake musi wykonać `do_deploy` naszej recepty.
Bez tego nikt nie zbudowałby `kurs-overlays` i pliku nie byłoby w katalogu deploy.

**config.txt.** Plik generuje recepta `rpi-config` z meta-raspberrypi, a dodatkowe linie bierze ze zmiennej
`RPI_EXTRA_CONFIG`. Utwórz `~/yocto-kurs/meta-kurs/recipes-bsp/bootfiles/rpi-config_git.bbappend`:

    # nasze overlaye w config.txt
    RPI_EXTRA_CONFIG:append = "\ndtoverlay=kurs-led"

`\n` zamienia się w nową linię dopiero przy generowaniu `config.txt` (recepta wypisuje zmienną przez `printf`).

Zbuduj obraz i sprawdź wynik bez flashowania:

    # kontener
    bitbake core-image-base
    tail -n 3 /workdir/bitbake-builds/rpi4/build/tmp/deploy/images/raspberrypi4-64/bootfiles/config.txt

Obraz wgramy w następnym ćwiczeniu, razem z nowym kernelem. Zapisz zmiany w git (na hoście):

    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-overlays: dioda LED na GPIO23"

## Źródła

- [Raspberry Pi Documentation - Device Trees, overlays, and parameters](https://www.raspberrypi.com/documentation/computers/configuration.html#device-trees-overlays-and-parameters)
- [Yocto Reference - devicetree](https://docs.yoctoproject.org/wrynose/ref-manual/classes.html#ref-classes-devicetree)
- [meta-raspberrypi - Manual additions to config.txt](https://github.com/agherzan/meta-raspberrypi/blob/wrynose/docs/extra-build-config.md#manual-additions-to-configtxt)
- [Yocto Reference - IMAGE_BOOT_FILES](https://docs.yoctoproject.org/wrynose/ref-manual/variables.html#term-IMAGE_BOOT_FILES)
- [Linux kernel - LED handling under Linux](https://docs.kernel.org/leds/leds-class.html)
