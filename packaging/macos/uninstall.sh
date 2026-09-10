#!/usr/bin/env bash
# ==============================================================================
# Gothwad TV Browser — macOS Uninstaller
# ==============================================================================

set -e

APP_PATH="/Applications/Gothwad TV Browser.app"

if [ -d "$APP_PATH" ]; then
    echo "Removing Gothwad TV Browser from /Applications..."
    if [ -w "/Applications" ]; then
        rm -rf "$APP_PATH"
    else
        sudo rm -rf "$APP_PATH"
    fi
    echo "✓ Gothwad TV Browser has been uninstalled."
else
    echo "Gothwad TV Browser is not installed in /Applications."
fi
