@echo off
echo Building Library Management System...

REM Set JavaFX path - you need to download JavaFX SDK and update this path
set JAVAFX_PATH=C:\App\javafx-sdk-24\lib

REM Create output directory
if not exist "out" mkdir out

REM Compile Java files
echo Compiling Java files...
javac -cp "lib/*;%JAVAFX_PATH%\*" -d out src/application/*.java

if %ERRORLEVEL% NEQ 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!

REM Run the application
echo Starting Library Management System...
java -cp "out;lib/*;%JAVAFX_PATH%\*" --module-path "%JAVAFX_PATH%" --add-modules javafx.controls,javafx.fxml application.LibraryApp

pause 