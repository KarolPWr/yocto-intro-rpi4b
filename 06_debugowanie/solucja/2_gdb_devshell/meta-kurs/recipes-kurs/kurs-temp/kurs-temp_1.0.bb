SUMMARY = "Temperatura z czujnika IIO - program do debugowania, kurs Yocto"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://kurs-temp.c"

S = "${UNPACKDIR}"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} kurs-temp.c -o kurs-temp
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 kurs-temp ${D}${bindir}
}
