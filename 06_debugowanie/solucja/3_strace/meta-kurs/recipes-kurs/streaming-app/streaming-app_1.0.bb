SUMMARY = "Aplikacja z licencją - program do ćwiczenia ze strace, kurs Yocto"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://streaming-app.c \
    file://streaming-app.conf \
"

S = "${UNPACKDIR}"

do_compile() {
    ${CC} ${CFLAGS} ${LDFLAGS} streaming-app.c -o streaming-app
}

do_install() {
    install -d ${D}${bindir}
    install -m 0755 streaming-app ${D}${bindir}

    # systemd-tmpfiles tworzy plik licencji przy każdym starcie
    install -d ${D}${sysconfdir}/tmpfiles.d
    install -m 0644 ${UNPACKDIR}/streaming-app.conf ${D}${sysconfdir}/tmpfiles.d/
}
