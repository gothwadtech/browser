#!/usr/bin/env bash
# ==============================================================================
# Script to build Linux packages (.deb and .tar.gz) for Gothwad TV Browser
# ==============================================================================

set -e

APK_PATH="$1"
VERSION="$2"
OUT_DIR="$3"

if [ -z "$APK_PATH" ] || [ ! -f "$APK_PATH" ]; then
    echo "Usage: $0 <path_to_apk> <version> <output_directory>"
    exit 1
fi

[ -z "$VERSION" ] && VERSION="1.0.0"
[ -z "$OUT_DIR" ] && OUT_DIR="output_release"

mkdir -p "$OUT_DIR"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WORK_DIR=$(mktemp -d /tmp/gothwad_linux_build_XXXXXX)
trap 'rm -rf "$WORK_DIR"' EXIT

echo "Building Linux packages for version $VERSION..."

# ------------------------------------------------------------------------------
# 1. Build .deb package
# ------------------------------------------------------------------------------
echo "Creating Debian / Ubuntu (.deb) package..."
DEB_DIR="$WORK_DIR/deb_root"
mkdir -p "$DEB_DIR/DEBIAN"
mkdir -p "$DEB_DIR/usr/bin"
mkdir -p "$DEB_DIR/usr/share/applications"
mkdir -p "$DEB_DIR/usr/share/pixmaps"
mkdir -p "$DEB_DIR/usr/share/icons/hicolor/192x192/apps"
mkdir -p "$DEB_DIR/usr/share/gothwad-browser"

# Copy files
cp "$SCRIPT_DIR/bin/gothwad-browser" "$DEB_DIR/usr/bin/gothwad-browser"
chmod +x "$DEB_DIR/usr/bin/gothwad-browser"

cp "$SCRIPT_DIR/gothwad-browser.desktop" "$DEB_DIR/usr/share/applications/gothwad-browser.desktop"
chmod 644 "$DEB_DIR/usr/share/applications/gothwad-browser.desktop"

if [ -f "$SCRIPT_DIR/icon.png" ]; then
    cp "$SCRIPT_DIR/icon.png" "$DEB_DIR/usr/share/pixmaps/gothwad-browser.png"
    cp "$SCRIPT_DIR/icon.png" "$DEB_DIR/usr/share/icons/hicolor/192x192/apps/gothwad-browser.png"
fi

cp "$APK_PATH" "$DEB_DIR/usr/share/gothwad-browser/app.apk"
chmod 644 "$DEB_DIR/usr/share/gothwad-browser/app.apk"

# DEBIAN/control
# Normalize version for deb (must start with digit, lowercase, etc.)
DEB_VERSION=$(echo "$VERSION" | sed 's/^v//')
cat <<EOF > "$DEB_DIR/DEBIAN/control"
Package: gothwad-browser
Version: $DEB_VERSION
Section: web
Priority: optional
Architecture: all
Depends: bash, waydroid | android-tools-adb
Recommends: zenity | kdialog
Maintainer: Gothwad TV Browser Team <support@gothwad.com>
Description: Gothwad TV Browser for Linux & Android
 Fast, modern, tabbed web browser designed for TV, Tablets, Phones, and Linux.
 Includes full D-pad remote navigation, keyboard shortcuts, mouse gestures,
 ad blocking, TV notes, downloads, and custom clipboard management.
EOF

# DEBIAN/postinst
cat <<'EOF' > "$DEB_DIR/DEBIAN/postinst"
#!/bin/sh
set -e
if command -v update-desktop-database >/dev/null 2>&1; then
    update-desktop-database /usr/share/applications || true
fi
if command -v gtk-update-icon-cache >/dev/null 2>&1; then
    gtk-update-icon-cache -f -t /usr/share/icons/hicolor || true
fi
exit 0
EOF
chmod 755 "$DEB_DIR/DEBIAN/postinst"

# DEBIAN/prerm
cat <<'EOF' > "$DEB_DIR/DEBIAN/prerm"
#!/bin/sh
set -e
exit 0
EOF
chmod 755 "$DEB_DIR/DEBIAN/prerm"

dpkg-deb --build "$DEB_DIR" "$OUT_DIR/gothwad-browser_${DEB_VERSION}_all.deb"
echo "✓ Built Debian package: $OUT_DIR/gothwad-browser_${DEB_VERSION}_all.deb"

# ------------------------------------------------------------------------------
# 2. Build .tar.gz universal standalone package
# ------------------------------------------------------------------------------
echo "Creating Universal Linux (.tar.gz) package..."
TAR_FOLDER_NAME="gothwad-browser-linux-v${VERSION}"
TAR_DIR="$WORK_DIR/tar_root/$TAR_FOLDER_NAME"
mkdir -p "$TAR_DIR"

cp "$SCRIPT_DIR/bin/gothwad-browser" "$TAR_DIR/gothwad-browser"
chmod +x "$TAR_DIR/gothwad-browser"

cp "$SCRIPT_DIR/gothwad-browser.desktop" "$TAR_DIR/gothwad-browser.desktop"
cp "$SCRIPT_DIR/install.sh" "$TAR_DIR/install.sh"
chmod +x "$TAR_DIR/install.sh"

cp "$SCRIPT_DIR/uninstall.sh" "$TAR_DIR/uninstall.sh"
chmod +x "$TAR_DIR/uninstall.sh"

if [ -f "$SCRIPT_DIR/icon.png" ]; then
    cp "$SCRIPT_DIR/icon.png" "$TAR_DIR/icon.png"
fi

cp "$APK_PATH" "$TAR_DIR/gothwad-browser.apk"

cat <<EOF > "$TAR_DIR/README.md"
# Gothwad TV Browser for Linux

Fast, modern, tabbed web browser for TV, Tablets, Phones, and Linux.

## Installation on Linux

Run the included installer:
\`\`\`bash
# System-wide installation (recommended):
sudo ./install.sh

# Or current user only:
./install.sh --user
\`\`\`

## Running
After installation, you can launch **Gothwad TV Browser** from your Linux application menu, or run from terminal:
\`\`\`bash
gothwad-browser
\`\`\`

## Requirements
Gothwad Browser runs natively on Linux with hardware acceleration via Waydroid.
- Ubuntu / Debian / Mint: \`sudo apt install waydroid\`
- Fedora: \`sudo dnf install waydroid\`
- Arch / Manjaro: \`sudo pacman -S waydroid\`

## Uninstallation
\`\`\`bash
sudo ./uninstall.sh
\`\`\`
EOF

tar -czf "$OUT_DIR/gothwad-browser-linux-v${VERSION}.tar.gz" -C "$WORK_DIR/tar_root" "$TAR_FOLDER_NAME"
echo "✓ Built Universal Tarball: $OUT_DIR/gothwad-browser-linux-v${VERSION}.tar.gz"

echo "=========================================================="
echo "Linux packages successfully generated in $OUT_DIR:"
ls -lh "$OUT_DIR"/*linux* "$OUT_DIR"/*.deb 2>/dev/null || true
echo "=========================================================="
