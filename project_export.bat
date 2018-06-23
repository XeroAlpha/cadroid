@echo off
set JDK_PATH=C:\Program Files\Java\jdk1.8.0_171\bin
set CORE_PATH=..\ca
set LCD=%CD%
echo Preparing core...
cd /d %CORE_PATH%
node script_export.js
cd  /d %LCD%
echo Encrypting Core...
node updateCore.js %CORE_PATH% app\signatures\release.signature
echo Compiling...
cmd /C gradlew assembleRelease
echo Signing...
"%JDK_PATH%\jarsigner.exe" -verbose -keystore ..\publish.keystore -signedjar .\app\release\app-release.apk .\app\build\outputs\apk\release\app-release-unsigned.apk appkey
echo Done.
echo Package has been exported to app\release
pause