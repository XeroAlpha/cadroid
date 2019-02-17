@echo off
set JDK_PATH=C:\Program Files\Java\jdk1.8.0_171\bin
set BUILD_TOOL_PATH=C:\Users\Administrator\AppData\Local\Android\Sdk\build-tools\28.0.0
set CORE_PATH=..\ca
set LCD=%CD%
echo Preparing core...
cd /d %CORE_PATH%
node script_export.js
cd  /d %LCD%
echo Compiling...
cmd /C gradlew :app:buildRelease
node updateCore.js release %CORE_PATH% app\signatures\release.signature
echo Assembling...
cmd /C gradlew :app:assembleRelease
echo Signing...
"%JDK_PATH%\jarsigner.exe" -verbose -keystore ..\publish.keystore -signedjar .\app\build\outputs\apk\release\app-release-unaligned.apk .\app\build\outputs\apk\release\app-release-unsigned.apk appkey<app\signatures\release.password>nul
echo Aligning...
"%BUILD_TOOL_PATH%\zipalign.exe" -f 4 .\app\build\outputs\apk\release\app-release-unaligned.apk .\app\release\app-release.apk
node updateCore.js export %CORE_PATH% app\signatures\release.signature
echo Done.
echo Package has been exported to app\release