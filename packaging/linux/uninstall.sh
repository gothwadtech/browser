#!/usr/bin/env bash
# ==============================================================================
# Gothwad TV Browser - Linux Uninstaller Script
# ==============================================================================

set -e

BINARY_NAME="gothwad-browser"

USER_MODE=false
if [ "$1" = "--user" ] || [ "$EUID" -ne 0 ]; then
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

echo "Removing Gothwad TV Browser from Linux..."

rm -f "$BIN_DIR/$BINARY_NAME"
rm -f "$APP_DIR/gothwad-browser.desktop"
rm -f "$ICON_DIR/gothwad-browser.png"
[ -n "$PIXMAPS_DIR" ] && rm -f "$PIXMAPS_DIR/gothwad-browser.png"
rm -rf "$DATA_DIR"

if command -v update-desktop-database >/dev/null 2>&1; then
    update-desktop-database "$APP_DIR" 2>/dev/null || true
fi

echo "✓ Uninstallation complete."
