@echo off
set CORE_PATH=%CD%\..\ca
set SHELL_PATH=%CD%
cd /d %CORE_PATH%\tools
node main shellPrepareDebug %CORE_PATH% %SHELL_PATH% %SHELL_PATH%\app\signatures\debug.signature
echo Package has been prepared for debuging.
cd /d %SHELL_PATH%