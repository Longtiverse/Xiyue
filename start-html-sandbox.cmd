@echo off
setlocal
cd /d %~dp0
start "Xiyue Sandbox Browser" cmd /c npm run sandbox
ping 127.0.0.1 -n 3 > nul
node scripts/open-html-sandbox.js
