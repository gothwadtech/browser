# Gothwad TV Browser - Linux Support & Installation

Gothwad TV Browser provides full Linux desktop support, running natively with hardware acceleration, tabbed browsing, ad-blocking, customizable keyboard shortcuts, and full mouse/touch navigation.

---

## 1. Quick Installation (Debian / Ubuntu / Linux Mint)

If you downloaded the `.deb` package:
```bash
sudo dpkg -i gothwad-browser_*_all.deb || sudo apt-get install -f -y
```

---

## 2. Universal Installation (Fedora, Arch, Manjaro, openSUSE, SteamOS)

If you downloaded the universal `.tar.gz` package:
```bash
# 1. Extract the tarball
tar -xzf gothwad-browser-linux-v*.tar.gz
cd gothwad-browser-linux-v*

# 2. Run the installer (System-wide)
sudo ./install.sh

# Or install for current user only:
./install.sh --user
```

---

## 3. Running the Browser

Once installed, **Gothwad TV Browser** will appear in your desktop application menu under the **Internet / Web Browser** category.

You can also launch it anytime from your terminal:
```bash
gothwad-browser
```

Or open a specific URL:
```bash
gothwad-browser https://google.com
```

---

## 4. Prerequisites

Gothwad Browser runs with full hardware acceleration on Linux using **Waydroid**.

### Installing Waydroid on your distribution:

- **Ubuntu / Debian / Mint**:
  ```bash
  sudo apt update
  sudo apt install waydroid
  ```

- **Fedora**:
  ```bash
  sudo dnf install waydroid
  ```

- **Arch Linux / Manjaro**:
  ```bash
  sudo pacman -S waydroid
  ```

- **Initialization (first time only)**:
  ```bash
  sudo waydroid init
  sudo systemctl enable --now waydroid-container
  ```

Once Waydroid is initialized, Gothwad Browser will automatically install and launch its package seamlessly.

---

## 5. Uninstallation

To remove Gothwad Browser:
```bash
# If installed via .deb:
sudo apt remove gothwad-browser

# If installed via tarball:
sudo ./uninstall.sh
```
