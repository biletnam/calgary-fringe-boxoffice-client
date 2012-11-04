@echo off
"\Program Files (x86)\Java\jdk1.6.0_33\bin\javac.exe" *.java
if errorlevel 1 (
    echo Compilation Error: %errorlevel%
) else (
    "\Program Files (x86)\Java\jdk1.6.0_33\bin\jar.exe" cfm BoxOffice.jar BoxOffice.manifest *.class
    echo Done
)