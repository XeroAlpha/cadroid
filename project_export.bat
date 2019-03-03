@echo off
set CORE_PATH=%CD%\..\ca
set SHELL_PATH=%CD%
cd /d %CORE_PATH%\tools
node main shellBuildRelease %CORE_PATH% %SHELL_PATH% %SHELL_PATH%\app\signatures\release.signature
cd /d %SHELL_PATH%