#!/usr/bin/env bash
# ==============================================================================
# Script to build Windows Portable / Setup Package (.zip) for Gothwad TV Browser
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
WORK_DIR=$(mktemp -d /tmp/gothwad_win_build_XXXXXX)
trap 'rm -rf "$WORK_DIR"' EXIT

echo "Building Windows package for version $VERSION..."

PACKAGE_DIR_NAME="gothwad-browser-windows-$VERSION"
STAGE_DIR="$WORK_DIR/$PACKAGE_DIR_NAME"
mkdir -p "$STAGE_DIR"

# Copy package contents
cp "$SCRIPT_DIR/gothwad-browser.bat" "$STAGE_DIR/"
cp "$SCRIPT_DIR/gothwad-browser.vbs" "$STAGE_DIR/"
cp "$SCRIPT_DIR/Install.bat" "$STAGE_DIR/"
cp "$SCRIPT_DIR/Uninstall.bat" "$STAGE_DIR/"
cp "$SCRIPT_DIR/icon.ico" "$STAGE_DIR/"
cp "$SCRIPT_DIR/README.md" "$STAGE_DIR/"
cp "$APK_PATH" "$STAGE_DIR/app.apk"

ZIP_NAME="gothwad-browser-windows-${VERSION}.zip"
ZIP_OUT_PATH="$(cd "$OUT_DIR" && pwd)/$ZIP_NAME"

# Build ZIP archive using python3
python3 -c "
import zipfile, os, sys

work_dir = sys.argv[1]
pkg_name = sys.argv[2]
out_zip = sys.argv[3]

with zipfile.ZipFile(out_zip, 'w', zipfile.ZIP_DEFLATED) as zf:
    base_path = os.path.join(work_dir, pkg_name)
    for root, dirs, files in os.walk(base_path):
        for f in files:
            full_path = os.path.join(root, f)
            rel_path = os.path.relpath(full_path, work_dir)
            zf.write(full_path, rel_path)
print(f'Successfully created {out_zip}')
" "$WORK_DIR" "$PACKAGE_DIR_NAME" "$ZIP_OUT_PATH"

echo "✓ Built Windows Package: $ZIP_OUT_PATH"
