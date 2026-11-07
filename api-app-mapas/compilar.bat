@echo off
title Compilando backend
cd /d "C:\Users\bloqu\OneDrive\Desktop\APPMOVIL\api-app-mapas"

rem OneDrive puede dejar archivos generados de target como solo lectura.
rem Maven necesita poder eliminarlos al ejecutar clean.
if exist "target" attrib -R "target\*" /S /D

call mvnw.cmd -DskipTests clean package
pause