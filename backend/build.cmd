@echo off

echo ========================================
echo Currency Exchange Service - Build Script
echo ========================================
echo.

if "%JAVA_HOME%" == "" (
    echo [ERROR] JAVA_HOME not set. Please install Java 21.
    exit /b 1
)

if not exist "%JAVA_HOME%\bin\java.exe" (
    echo [ERROR] Java not found at: %JAVA_HOME%
    exit /b 1
)

echo [OK] Java found: %JAVA_HOME%
echo.

echo [1/2] Building application...
"%JAVA_HOME%\bin\java" -classpath ".mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=%cd%" org.apache.maven.wrapper.MavenWrapperMain clean install -T 1C -DskipTests -q
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed
    exit /b %ERRORLEVEL%
)

echo [OK] Build successful
echo.
echo [2/2] Starting application...
echo ========================================
echo Server will start at: http://localhost:8080
echo H2 Console: http://localhost:8080/h2-console
echo Press Ctrl+C to stop
echo ========================================
echo.

"%JAVA_HOME%\bin\java" -classpath ".mvn\wrapper\maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=%cd%" org.apache.maven.wrapper.MavenWrapperMain spring-boot:run

