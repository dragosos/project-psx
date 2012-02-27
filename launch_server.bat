@echo off
color b
start /b launch_world.bat
timeout 5 >nul
start /b launch_login.bat
timeout 5 >nul
start /b launch_channel.bat