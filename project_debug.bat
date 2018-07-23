@echo off
set CORE_PATH=..\ca
node updateCore.js debug %CORE_PATH% app\signatures\debug.signature
echo Package has been prepared for debuging.