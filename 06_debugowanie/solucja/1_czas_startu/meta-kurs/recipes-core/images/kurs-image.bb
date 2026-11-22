SUMMARY = "Obraz kursu Yocto na Raspberry Pi 4B"
LICENSE = "MIT"

# wszystko z core-image-base
require recipes-core/images/core-image-base.bb

# serwer SSH
IMAGE_FEATURES += "ssh-server-dropbear"

# bez ekranu startowego psplash
IMAGE_FEATURES:remove = "splash"

# nasze programy
IMAGE_INSTALL += "packagegroup-kurs"

# nazwa hosta tylko w tym obrazie (base-files ustawia ${MACHINE})
kurs_hostname() {
    echo "kurs" > ${IMAGE_ROOTFS}${sysconfdir}/hostname
    sed -i 's/^127\.0\.1\.1 .*/127.0.1.1 kurs/' ${IMAGE_ROOTFS}${sysconfdir}/hosts
}
ROOTFS_POSTPROCESS_COMMAND += "kurs_hostname"

# serwisy, których płytka nie potrzebuje
KURS_MASKED_SERVICES = " \
    ofono.service \
    avahi-daemon.service \
    avahi-daemon.socket \
    bluetooth.service \
    hciuart.service \
    rpcbind.service \
    rpcbind.socket \
"

kurs_mask_services() {
    for s in ${KURS_MASKED_SERVICES}; do
        systemctl --root=${IMAGE_ROOTFS} mask $s
    done
}
ROOTFS_POSTPROCESS_COMMAND += "kurs_mask_services"
