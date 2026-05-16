FILESEXTRAPATHS_prepend := "${THISDIR}/files:"
SRC_URI += "file://wpa_supplicant.conf"

do_install_append() {
    install -d ${D}${sysconfdir}
    install -m 0644 ${WORKDIR}/wpa_supplicant.conf ${D}${sysconfdir}/wpa_supplicant.conf
}
