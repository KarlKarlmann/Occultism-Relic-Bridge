@echo off
setlocal enabledelayedexpansion

:: --- 1. PFADE DYNAMISCH ERMITTELN ---
:: Nutzt das aktuelle Verzeichnis, aus dem die Batch gestartet wird
set "MOD_DIR=%CD%"

:: Holt den Mod-Namen aus dem aktuellen Ordnernamen
for %%I in ("%MOD_DIR%") do set "MOD_NAME=%%~nxI"

:: Holt das Hauptverzeichnis (einen Ordner nach oben)
for %%I in ("%MOD_DIR%\..") do set "BASE_DIR=%%~fI"

set "GITHUB_DIR=%BASE_DIR%\github"

echo ===================================================
echo Starte GitHub-Upload fuer: %MOD_NAME%
echo ===================================================

:: --- 2. ORIGINAL BUILD.GRADLE SICHERN ---
if exist "%MOD_DIR%\build.gradle" (
    echo [INFO] Sichere originale build.gradle...
    copy /Y "%MOD_DIR%\build.gradle" "%MOD_DIR%\build.gradle.backup" >nul
)

:: --- 3. WRAPPER AUS DEM HAUPTVERZEICHNIS HOLEN ---
echo [INFO] Hole Gradle-Wrapper aus dem Hauptverzeichnis...
copy /Y "%BASE_DIR%\gradlew" "%MOD_DIR%\" >nul
copy /Y "%BASE_DIR%\gradlew.bat" "%MOD_DIR%\" >nul
xcopy "%BASE_DIR%\gradle" "%MOD_DIR%\gradle" /I /E /Y /Q >nul

:: --- 4. GEMEINSAME DATEIEN AUS \github KOPIEREN ---
if exist "%GITHUB_DIR%" (
    echo [INFO] Kopiere geteilte GitHub-Dateien aus \github...
    xcopy "%GITHUB_DIR%\*" "%MOD_DIR%\" /Y /E /Q >nul
)

:: --- 5. BUILD.GRADLE FUER GITHUB ZUSAMMENBAUEN ---
:: Wenn im \github Ordner ein "build_header.gradle" liegt (mit den Forge-Plugins etc.),
:: wird dieser hier temporär VOR deine mod-spezifische build.gradle geklebt.
if exist "%MOD_DIR%\build_header.gradle" (
    echo [INFO] Fuege build_header.gradle und Mod-build.gradle zusammen...
    copy /b "%MOD_DIR%\build_header.gradle" + "%MOD_DIR%\build.gradle.backup" "%MOD_DIR%\build.gradle" >nul
    :: Loesche den Header aus dem Mod-Ordner, damit er nicht als extra Datei auf Git landet
    del /Q "%MOD_DIR%\build_header.gradle"
)

:: --- 6. GIT BEFEHLE AUSFUEHREN ---
echo [INFO] Wechsle in das Mod-Verzeichnis und starte Git...
cd /d "%MOD_DIR%"

git add .
git commit -m "Automatischer Standalone-Build Upload %DATE% %TIME%"
git push

:: --- 7. AUFRAEUMEN (LOKAL ALLES WIE VORHER MACHEN) ---
echo [INFO] Upload abgeschlossen. Raeume temporaere Dateien wieder auf...

:: Wrapper wieder löschen
if exist "gradlew" del /Q "gradlew"
if exist "gradlew.bat" del /Q "gradlew.bat"
if exist "gradle" rmdir /S /Q "gradle"

:: Alle Dateien, die aus dem \github Ordner kamen, wieder loeschen
for %%F in ("%GITHUB_DIR%\*") do (
    if exist "%MOD_DIR%\%%~nxF" del /Q "%MOD_DIR%\%%~nxF"
)

:: Originale build.gradle wiederherstellen
if exist "build.gradle.backup" (
    move /Y "build.gradle.backup" "build.gradle" >nul
)

echo ===================================================
echo [ERFOLG] Mod %MOD_NAME% ist auf GitHub und lokal ist alles beim Alten!
echo ===================================================
pause