SUMMARY = "Prosty moduł jądra - kurs Yocto"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

inherit module kurs-info

SRC_URI = "file://Makefile \
           file://kurs.c \
          "

S = "${UNPACKDIR}"

# paczka z modułem nazywa się kernel-module-kurs-<wersja kernela>
RPROVIDES:${PN} += "kernel-module-kurs"

# ładowanie modułu przy starcie systemu, z parametrem
KERNEL_MODULE_AUTOLOAD += "kurs"
KERNEL_MODULE_PROBECONF += "kurs"
module_conf_kurs = "options kurs imie=Jan"
