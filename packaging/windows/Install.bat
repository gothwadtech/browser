@echo off
setlocal enabledelayedexpansion
title Gothwad TV Browser Installer

echo =========================================================
echo       Installing Gothwad TV Browser on Windows...
echo =========================================================
echo.

set "INSTALL_DIR=%LOCALAPPDATA%\GothwadBrowser"
set "SOURCE_DIR=%~dp0"

:: Create install directory
if not exist "%INSTALL_DIR%" mkdir "%INSTALL_DIR%"

:: Copy files
echo Copying application files to %INSTALL_DIR%...
copy /y "%SOURCE_DIR%gothwad-browser.bat" "%INSTALL_DIR%\" >nul
copy /y "%SOURCE_DIR%gothwad-browser.vbs" "%INSTALL_DIR%\" >nul
copy /y "%SOURCE_DIR%icon.ico" "%INSTALL_DIR%\" >nul
copy /y "%SOURCE_DIR%Uninstall.bat" "%INSTALL_DIR%\" >nul
if exist "%SOURCE_DIR%app.apk" copy /y "%SOURCE_DIR%app.apk" "%INSTALL_DIR%\" >nul

:: Create Start Menu and Desktop Shortcuts via PowerShell
echo Creating Start Menu and Desktop shortcuts...
powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ws = New-Object -ComObject WScript.Shell;" ^
    "$desktop = [Environment]::GetFolderPath('Desktop');" ^
    "$startMenu = [Environment]::GetFolderPath('Programs');" ^
    "$target = 'wscript.exe';" ^
    "$args = '\"' + $env:LOCALAPPDATA + '\GothwadBrowser\gothwad-browser.vbs\" \"' + $env:LOCALAPPDATA + '\GothwadBrowser\gothwad-browser.bat\"';" ^
    "$icon = $env:LOCALAPPDATA + '\GothwadBrowser\icon.ico';" ^
    "$s1 = $ws.CreateShortcut($desktop + '\Gothwad TV Browser.lnk');" ^
    "$s1.TargetPath = $target;" ^
    "$s1.Arguments = $args;" ^
    "$s1.IconLocation = $icon;" ^
    "$s1.Description = 'Fast, tabbed web browser for TV, PC and Android';" ^
    "$s1.Save();" ^
    "$s2 = $ws.CreateShortcut($startMenu + '\Gothwad TV Browser.lnk');" ^
    "$s2.TargetPath = $target;" ^
    "$s2.Arguments = $args;" ^
    "$s2.IconLocation = $icon;" ^
    "$s2.Description = 'Fast, tabbed web browser for TV, PC and Android';" ^
    "$s2.Save();"

:: Check if WSA or ADB is ready to install APK immediately
set "WSA_CLIENT=%LOCALAPPDATA%\Microsoft\WindowsApps\MicrosoftCorporationII.WindowsSubsystemForAndroid_8wekyb3d8bbwe\WsaClient.exe"
set "APK_FILE=%INSTALL_DIR%\app.apk"

where adb >nul 2>&1
if %errorlevel% equ 0 (
    echo Checking Android Subsystem / ADB connection...
    adb connect 127.0.0.1:58526 >nul 2>&1
    if exist "%APK_FILE%" (
        adb install -r "%APK_FILE%" >nul 2>&1
        if %errorlevel% equ 0 (
            echo App installed successfully to Android Subsystem!
        )
    )
)

echo.
echo =========================================================
echo  Installation Complete!
echo.
echo  - Desktop shortcut created: "Gothwad TV Browser"
echo  - Start Menu shortcut created
echo =========================================================
echo.
pause
