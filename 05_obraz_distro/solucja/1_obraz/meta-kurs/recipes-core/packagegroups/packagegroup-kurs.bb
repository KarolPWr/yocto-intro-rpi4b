SUMMARY = "Programy i narzędzia z kursu Yocto"

inherit packagegroup

RDEPENDS:${PN} = " \
    hello \
    nudoku \
    bme280-log \
    kurs-mod \
    i2c-tools \
    python3-flask \
"
