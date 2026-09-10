#!/usr/bin/env bash
# ==============================================================================
# Script to build macOS Application Package (.app bundle in .zip) for Gothwad TV Browser
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
WORK_DIR=$(mktemp -d /tmp/gothwad_macos_build_XXXXXX)
trap 'rm -rf "$WORK_DIR"' EXIT

echo "Building macOS package for version $VERSION..."

PACKAGE_DIR_NAME="gothwad-browser-macos-$VERSION"
STAGE_DIR="$WORK_DIR/$PACKAGE_DIR_NAME"
APP_DIR="$STAGE_DIR/Gothwad TV Browser.app"

mkdir -p "$APP_DIR/Contents/MacOS"
mkdir -p "$APP_DIR/Contents/Resources"

# 1. Populate Info.plist with current version
sed "s/<string>1.0.0<\/string>/<string>$VERSION<\/string>/g" "$SCRIPT_DIR/Info.plist" > "$APP_DIR/Contents/Info.plist"

# 2. Executable launcher
cp "$SCRIPT_DIR/bin/gothwad-browser" "$APP_DIR/Contents/MacOS/gothwad-browser"
chmod 755 "$APP_DIR/Contents/MacOS/gothwad-browser"

# 3. Resources
cp "$SCRIPT_DIR/AppIcon.icns" "$APP_DIR/Contents/Resources/AppIcon.icns"
cp "$APK_PATH" "$APP_DIR/Contents/Resources/app.apk"
chmod 644 "$APP_DIR/Contents/Resources/app.apk"

# 4. Helper scripts & docs
cp "$SCRIPT_DIR/install.sh" "$STAGE_DIR/install.sh"
chmod 755 "$STAGE_DIR/install.sh"

cp "$SCRIPT_DIR/uninstall.sh" "$STAGE_DIR/uninstall.sh"
chmod 755 "$STAGE_DIR/uninstall.sh"

cp "$SCRIPT_DIR/README.md" "$STAGE_DIR/README.md"

ZIP_NAME="gothwad-browser-macos-${VERSION}.zip"
ZIP_OUT_PATH="$(cd "$OUT_DIR" && pwd)/$ZIP_NAME"

# Build ZIP archive using python3 with UNIX permissions preserved
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
            st = os.stat(full_path)
            info = zipfile.ZipInfo(rel_path)
            info.date_time = (2026, 1, 1, 0, 0, 0)
            # Store UNIX file permissions (especially executable bit 0o755)
            info.external_attr = (st.st_mode & 0xFFFF) << 16
            with open(full_path, 'rb') as fp:
                zf.writestr(info, fp.read())
print(f'Successfully created {out_zip}')
" "$WORK_DIR" "$PACKAGE_DIR_NAME" "$ZIP_OUT_PATH"

echo "✓ Built macOS Package: $ZIP_OUT_PATH"
