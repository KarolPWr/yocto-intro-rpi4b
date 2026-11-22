SUMMARY = "Log temperatury z czujnika BME280 - kurs Yocto"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://bme280-log.sh \
           file://bme280-log.service \
          "

S = "${UNPACKDIR}"

inherit systemd

# serwis w paczce bme280-log, włączony przy starcie (SYSTEMD_AUTO_ENABLE domyślnie "enable")
SYSTEMD_SERVICE:${PN} = "bme280-log.service"

do_install() {
    install -d ${D}${bindir}
    install -m 0755 bme280-log.sh ${D}${bindir}/bme280-log

    install -d ${D}${systemd_system_unitdir}
    install -m 0644 bme280-log.service ${D}${systemd_system_unitdir}
}
