
# Z7-Nano Yocto / PetaLinux System Configuration Artifacts 

This project is based on a PetaLinux 2018.3 BSP targeting Xilinx Zynq-7020 (Z7-Nano) with extensive modifications across bootloader, kernel, device tree, and root filesystem layers to enable stable USB host operation, AR9271 Wi-Fi integration, and external storage support.

---

## System Scope & Constraints

This project was developed as a full embedded Linux bring-up on Zynq-7020 under strict constraints of minimal root filesystem size, deterministic boot sequencing, and reliable USB peripheral initialization.

The system integrates hardware configuration, kernel-level driver enablement, device tree customization, and user-space automation for USB storage and Wi-Fi connectivity.

---

# 1. Hardware Bring-up (Vivado / PS-PL)

The Zynq-7020 Processing System was configured to support stable USB Host operation via PS-PL integration.

- Enabled PS7 USB0 interface via dedicated MIO mapping
- Configured ULPI PHY interface for external USB transceiver communication
- Tuned FCLK_CLK0 and reset sequencing to guarantee USB controller initialization prior to Linux kernel enumeration
- Enabled SD boot path, UART debug channel, and GPIO banks for system-level diagnostics

---

# 2. Boot Image Composition (BOOT.BIN)

## FSBL (First Stage Boot Loader)
- Initializes DDR memory controller
- Configures PS-PL clocking (FCLK, MIO routing)
- Loads FPGA bitstream into PL region
- Performs early hardware initialization required for U-Boot handoff

## FPGA Bitstream (.bit)
- Custom Vivado block design for Zynq-7020
- USB0 controller enabled in PS7 configuration
- ULPI PHY interface configured for external USB transceiver
- SDIO and UART peripherals enabled for system boot and debugging

## U-Boot (Second Stage Boot Loader)
- SD card boot configuration (mmc0)
- Environment variables tuned for USB stability and device enumeration
- Kernel bootargs configured for ext4 root filesystem
- USB initialization delays adjusted for AR9271 detection reliability

---

# 3. Kernel Image (image.ub)

## Linux Kernel (PetaLinux 2018.3)
Target: Zynq-7020 ARM Cortex-A9

## Device Tree Blob (DTB)
- system.dtb generated from Vivado hardware description
- Extended via system-user.dtsi overlays

---

## Kernel Configuration Enhancements

### Wireless Stack
- CFG80211 (core wireless framework)
- MAC80211 (IEEE 802.11 subsystem)
- ATH_COMMON (Atheros shared driver layer)
- ATH9K / ATH9K_HTC (USB AR9271 support)
- MINSTREL_HT rate control (802.11n optimization)

### USB Subsystem
- USB_XHCI_HCD
- USB hotplug event handling
- Enhanced device enumeration support

### Cryptography
- AES
- CCM / GCM
- SHA256
- ARC4

---

# 4. Device Tree Configuration (BSP Layer)

## USB Host Enforcement
- dr_mode explicitly set to "host" for USB0 controller
- Overrides OTG/peripheral fallback behavior

## PHY Layer
- usb-nop-xceiv stub PHY introduced
- Ensures correct ULPI handover

## Node Stability
- status = "okay" enforced across USB-related nodes
- Guarantees deterministic probe order

- 
The following modifications were applied to enforce USB host mode and ULPI PHY integration on Zynq-7020:

```dts
#include "system-conf.dtsi"

/ {
    usb_phy0: usb-phy {
        compatible = "ulpi-phy";
        #phy-cells = <0>;
        drv-vbus;
    };
};

&usb0 {
    status = "okay";
    dr_mode = "host";
    phys = <&usb_phy0>;
    phy-names = "usb2-phy";
};

```

---


# 5. Boot-Time Determinism & Initialization Order

USB subsystem initialization was synchronized with kernel bring-up to avoid race conditions between:

- USB host controller probe
- PHY initialization (ULPI layer)
- Early device enumeration phase

This was critical for stable detection of USB storage and AR9271 devices during cold boot scenarios.

---

# 6. Engineering Challenges

- USB host instability during early boot due to PHY ordering issues (ULPI vs kernel probe)
- AR9271 firmware loading delays causing intermittent interface disappearance
- Device tree OTG default mode conflicts causing enumeration failures
- Root filesystem size constraints requiring aggressive driver/module stripping

---

# 7. Kernel Stack: AR9271 Wi-Fi Integration

Enabled subsystems:
- CFG80211 / MAC80211
- ATH9K / ATH9K_HTC
- MINSTREL_HT rate control

Cryptography:
- AES, CCM, GCM, SHA256, ARC4

USB enhancements:
- Hotplug event handling
- XHCI host controller support

---

# 8. Root Filesystem (RootFS)

## Base System
- PetaLinux Yocto-based root filesystem
- BusyBox core utilities

## System Utilities
- Midnight Commander (MC)
- pciutils
- can-utils
- mtd-utils

## Networking
- Avahi daemon (mDNS: petalinuxusb.local)
- WPA Supplicant (AR9271 automation profile)
- OpenSSH SFTP server

## Debug Tools
- GPIO utilities
- Hardware inspection scripts

---

# 9. Yocto / Custom Layers

## meta-ath9k-firmware
- AR9271 firmware blobs
- /lib/firmware/ath9k_htc/

## meta-peekpoke
- Zynq register-level debug tool

## meta-gpio-demo
- GPIO validation utilities

---

# 10. System Optimization (Size & Performance)

- Final image.ub reduced to <20MB
- Kernel modules stripped to minimum required set
- USB storage stack stability preserved under constrained footprint
- Faster USB device availability at boot

---

# 11. Boot Sequence (End-to-End Flow)

1. BootROM (Zynq immutable ROM)
2. FSBL:
   - DDR init
   - Clock setup
   - FPGA bitstream load
3. U-Boot:
   - SD boot (mmc0)
   - Kernel + DTB load
4. Linux kernel:
   - Device tree application
   - Driver initialization
5. RootFS mount (ext4)
6. Userspace:
   - USB host activation
   - AR9271 firmware load
   - WPA supplicant auto-connect
   - Avahi mDNS advertising

---

# 12. Boot Targets & Runtime Environment

- Boot device: SD card (mmcblk0)
- Root filesystem: ext4
- Network:
  - Ethernet primary
  - AR9271 Wi-Fi fallback
- Debug:
  - UART0 @ 115200
- Hostname:
  - petalinuxusb.local

---

# 13. System Constraints & Design Assumptions

- Deterministic USB host behavior prioritized
- Root filesystem <20MB constraint
- Single Wi-Fi adapter scenario
- USB hub topology not fully validated
- Stability over feature completeness

---

# 14. Known Limitations & Design Trade-offs

- Single Wi-Fi device scenario (no multi-adapter support)
- No OTA update mechanism
- USB hub topology not fully validated
- WPA2-PSK only (no enterprise auth)
- System optimized for deterministic behavior over scalability


