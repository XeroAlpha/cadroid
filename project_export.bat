@echo off
set JDK_PATH=C:\Program Files\Java\jdk1.8.0_171\bin
set CORE_PATH=..\ca
set LCD=%CD%
echo Preparing core...
cd /d %CORE_PATH%
node script_export.js
cd  /d %LCD%
echo Compiling...
cmd /C gradlew assembleRelease
node updateCore.js %CORE_PATH% app\signatures\release.signature
echo Assembling...
cmd /C gradlew assembleRelease
echo Signing...
"%JDK_PATH%\jarsigner.exe" -verbose -keystore ..\publish.keystore -signedjar .\app\release\app-release.apk .\app\build\outputs\apk\release\app-release-unsigned.apk appkey<app\signatures\release.password>nul
start explorer.exe .\app\release
echo Done.
echo Package has been exported to app\release
echo.
echo Press any key to exit.
pause>nul