# Rozwiązanie: Device Tree overlay - dioda LED

Pliki warstwy po ćwiczeniu:

    meta-kurs/
    ├── conf/fragments/kurs.conf
    ├── recipes-bsp/bootfiles/rpi-config_git.bbappend
    └── recipes-bsp/kurs-overlays/
        ├── files/kurs-led.dts
        └── kurs-overlays_1.0.bb

Overlay: [meta-kurs/recipes-bsp/kurs-overlays/files/kurs-led.dts](meta-kurs/recipes-bsp/kurs-overlays/files/kurs-led.dts)

Uzupełnione pola:
- `brcm,pins = <23>`, `brcm,function = <1>` (wyjście), `brcm,pull = <0>` (dioda ma własny obwód, podciąganie
  niepotrzebne),
- `status = "okay"` - węzeł włączony (`"disabled"` - kernel go pomija),
- `label = "kurs"` - nazwa diody w `/sys/class/leds/`,
- `gpios = <&gpio 23 0>` - kontroler GPIO, numer linii, flagi (0 = aktywny stan wysoki),
- `linux,default-trigger = "heartbeat"` - trigger ustawiany przy starcie (`CONFIG_LEDS_TRIGGER_HEARTBEAT=y`
  w kernelu RPi).

Źródła: [binding gpio-leds](https://github.com/raspberrypi/linux/blob/rpi-6.18.y/Documentation/devicetree/bindings/leds/leds-gpio.yaml),
raspberrypi/linux rpi-6.18.y `arch/arm/boot/dts/overlays/gpio-led-overlay.dts`.

## Odpowiedzi

**Bez `KERNEL_INCLUDE = ""`:** klasa `devicetree` domyślnie dodaje do ścieżek `#include` katalogi ze źródeł kernela.
Jeśli `KERNEL_INCLUDE` nie jest pusty, jej anonimowa funkcja Pythona dopisuje zależność
`do_compile[depends] += "virtual/kernel:do_configure"`. Kernel z sstate nie ma rozpakowanych źródeł, więc bitbake
musiałby pobrać (z `DL_DIR`), rozpakować i skonfigurować kernel - kilka minut i kilka GB na dysku tylko po to, żeby
skompilować overlay, który z tych źródeł nie korzysta. Gdyby overlay używał `#include <dt-bindings/...>`
(np. `GPIO_ACTIVE_HIGH` zamiast `0`), ta linia musiałaby zniknąć.

Źródło: openembedded-core wrynose `meta/classes-recipe/devicetree.bbclass`.

## Zadanie - dioda z konsoli U-Boota

Zatrzymaj autostart i:

    gpio set GPIO23
    gpio clear GPIO23
    gpio toggle GPIO23
    gpio status GPIO23

`GPIO` to nazwa banku sterownika GPIO w U-Boocie, `23` - numer linii (zadziała też samo `gpio set 23`). `gpio set`
sam ustawia pin jako wyjście. U-Boot nie korzysta z overlayu - o pinie wie tylko ze sterownika GPIO.

Źródła: U-Boot v2026.01 `cmd/gpio.c`, `drivers/gpio/gpio-uclass.c` (`dm_gpio_lookup_name`),
`drivers/gpio/bcm2835_gpio.c` (`bank_name = "GPIO"`); [U-Boot - gpio command](https://docs.u-boot.org/en/latest/usage/cmd/gpio.html).

## Stan po ćwiczeniu (jeśli zostałeś w tyle)

Na hoście:

    cp -r ~/yocto-kurs/yocto-intro-rpi4b/04_sprzet/solucja/1_led_overlay/meta-kurs/. ~/yocto-kurs/meta-kurs/
    cd ~/yocto-kurs/meta-kurs
    git add .
    git commit -m "kurs-overlays: dioda LED na GPIO23"

W kontenerze:

    # kontener
    bitbake core-image-base
