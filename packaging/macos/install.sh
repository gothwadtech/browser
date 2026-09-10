#!/usr/bin/env bash
# ==============================================================================
# Gothwad TV Browser — macOS Installer
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_BUNDLE="$SCRIPT_DIR/Gothwad TV Browser.app"
DEST_DIR="/Applications"

if [ ! -d "$APP_BUNDLE" ]; then
    echo "Error: 'Gothwad TV Browser.app' not found in $SCRIPT_DIR"
    exit 1
fi

echo "Installing Gothwad TV Browser to $DEST_DIR..."

if [ -w "$DEST_DIR" ]; then
    cp -R "$APP_BUNDLE" "$DEST_DIR/"
else
    sudo cp -R "$APP_BUNDLE" "$DEST_DIR/"
fi

# Remove quarantine attribute on macOS to prevent gatekeeper block
xattr -cr "$DEST_DIR/Gothwad TV Browser.app" 2>/dev/null || true

echo "✓ Installation complete! You can now launch Gothwad TV Browser from Launchpad or Spotlight."
