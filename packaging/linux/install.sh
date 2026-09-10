#!/usr/bin/env bash
# ==============================================================================
# Gothwad TV Browser - Linux Installation Script
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_NAME="Gothwad TV Browser"
BINARY_NAME="gothwad-browser"

USER_MODE=false
if [ "$1" = "--user" ] || [ "$EUID" -ne 0 ]; then
    if [ "$EUID" -ne 0 ] && [ "$1" != "--user" ]; then
        echo "Notice: Not running as root. Installing for current user only ($HOME/.local)..."
        echo "Tip: Run 'sudo ./install.sh' for a system-wide installation."
    fi
    USER_MODE=true
fi

if [ "$USER_MODE" = true ]; then
    BIN_DIR="$HOME/.local/bin"
    APP_DIR="$HOME/.local/share/applications"
    ICON_DIR="$HOME/.local/share/icons/hicolor/192x192/apps"
    DATA_DIR="$HOME/.local/share/gothwad-browser"
else
    BIN_DIR="/usr/local/bin"
    APP_DIR="/usr/share/applications"
    ICON_DIR="/usr/share/icons/hicolor/192x192/apps"
    PIXMAPS_DIR="/usr/share/pixmaps"
    DATA_DIR="/usr/share/gothwad-browser"
fi

echo "=========================================================="
echo " Installing $APP_NAME on Linux..."
echo "=========================================================="

mkdir -p "$BIN_DIR"
mkdir -p "$APP_DIR"
mkdir -p "$ICON_DIR"
mkdir -p "$DATA_DIR"
[ -n "$PIXMAPS_DIR" ] && mkdir -p "$PIXMAPS_DIR"

# 1. Install binary launcher
if [ -f "$SCRIPT_DIR/bin/$BINARY_NAME" ]; then
    cp "$SCRIPT_DIR/bin/$BINARY_NAME" "$BIN_DIR/$BINARY_NAME"
elif [ -f "$SCRIPT_DIR/$BINARY_NAME" ]; then
    cp "$SCRIPT_DIR/$BINARY_NAME" "$BIN_DIR/$BINARY_NAME"
fi
chmod +x "$BIN_DIR/$BINARY_NAME"
echo "✓ Installed executable: $BIN_DIR/$BINARY_NAME"

# 2. Install desktop shortcut
if [ -f "$SCRIPT_DIR/gothwad-browser.desktop" ]; then
    cp "$SCRIPT_DIR/gothwad-browser.desktop" "$APP_DIR/gothwad-browser.desktop"
    chmod 644 "$APP_DIR/gothwad-browser.desktop"
    echo "✓ Installed desktop shortcut: $APP_DIR/gothwad-browser.desktop"
fi

# 3. Install icons
if [ -f "$SCRIPT_DIR/icon.png" ]; then
    cp "$SCRIPT_DIR/icon.png" "$ICON_DIR/gothwad-browser.png"
    [ -n "$PIXMAPS_DIR" ] && cp "$SCRIPT_DIR/icon.png" "$PIXMAPS_DIR/gothwad-browser.png"
    echo "✓ Installed app icon to system icon theme"
fi

# 4. Install APK data
APK_SRC=$(find "$SCRIPT_DIR" -maxdepth 2 -name "*.apk" | head -n 1)
if [ -n "$APK_SRC" ] && [ -f "$APK_SRC" ]; then
    cp "$APK_SRC" "$DATA_DIR/app.apk"
    echo "✓ Installed browser bundle: $DATA_DIR/app.apk"
else
    echo "⚠️ Notice: No .apk found in installer directory; ensure app.apk is placed in $DATA_DIR"
fi

# 5. Update system caches
if command -v update-desktop-database >/dev/null 2>&1; then
    update-desktop-database "$APP_DIR" 2>/dev/null || true
fi
if command -v gtk-update-icon-cache >/dev/null 2>&1; then
    gtk-update-icon-cache -f -t /usr/share/icons/hicolor 2>/dev/null || true
fi

echo ""
echo "=========================================================="
echo " $APP_NAME has been successfully installed!"
echo " You can launch it from your application menu or run:"
echo "   $BINARY_NAME"
echo "=========================================================="
