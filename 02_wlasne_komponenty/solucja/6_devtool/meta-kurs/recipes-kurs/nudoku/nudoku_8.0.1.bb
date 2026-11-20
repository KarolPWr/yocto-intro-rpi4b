SUMMARY = "Sudoku w terminalu (ncurses)"
HOMEPAGE = "https://github.com/jubalh/nudoku"
LICENSE = "GPL-3.0-or-later"
LIC_FILES_CHKSUM = "file://LICENSE;md5=d32239bcb673463ab874e80d47fae504"

SRC_URI = "git://github.com/jubalh/nudoku.git;protocol=https;branch=master \
           file://0001-nudoku-add-name-to-help-text-h.patch \
           "
SRCREV = "96638addc1b5ec45293ed122ffb6b68248851115"

DEPENDS = "ncurses"

inherit autotools gettext pkgconfig
