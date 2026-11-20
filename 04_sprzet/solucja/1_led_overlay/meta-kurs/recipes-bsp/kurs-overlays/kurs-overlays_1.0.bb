SUMMARY = "Overlaye Device Tree - kurs Yocto"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

inherit devicetree

SRC_URI = "file://kurs-led.dts"

# tylko dla Raspberry Pi
COMPATIBLE_MACHINE = "^rpi$"

# overlaye nie używają nagłówków kernela (#include), więc nie potrzebujemy jego źródeł
KERNEL_INCLUDE = ""
