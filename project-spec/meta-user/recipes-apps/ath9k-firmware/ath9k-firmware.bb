SUMMARY = "Atheros AR9271 Firmware and Auto-Wifi Script"
SECTION = "PETALINUX/apps"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = "file://htc_9271.fw \
           file://wifi-init \
          "

S = "${WORKDIR}"

inherit update-rc.d

INITSCRIPT_NAME = "wifi-init"
INITSCRIPT_PARAMS = "defaults 99"

do_install() {
    # Установка прошивки
    install -d ${D}/lib/firmware
    install -m 0644 ${S}/htc_9271.fw ${D}/lib/firmware/htc_9271.fw

    # Установка скрипта автозапуска
    install -d ${D}${sysconfdir}/init.d
    install -m 0755 ${S}/wifi-init ${D}${sysconfdir}/init.d/wifi-init
}

FILES_${PN} += "/lib/firmware/htc_9271.fw ${sysconfdir}/init.d/wifi-init"

