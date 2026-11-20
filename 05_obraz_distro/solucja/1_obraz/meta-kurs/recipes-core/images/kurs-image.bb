SUMMARY = "Obraz kursu Yocto na Raspberry Pi 4B"
LICENSE = "MIT"

# wszystko z core-image-base
require recipes-core/images/core-image-base.bb

# serwer SSH
IMAGE_FEATURES += "ssh-server-dropbear"

# nasze programy
IMAGE_INSTALL += "packagegroup-kurs"

# nazwa hosta tylko w tym obrazie (base-files ustawia ${MACHINE})
kurs_hostname() {
    echo "kurs" > ${IMAGE_ROOTFS}${sysconfdir}/hostname
    sed -i 's/^127\.0\.1\.1 .*/127.0.1.1 kurs/' ${IMAGE_ROOTFS}${sysconfdir}/hosts
}
ROOTFS_POSTPROCESS_COMMAND += "kurs_hostname"
