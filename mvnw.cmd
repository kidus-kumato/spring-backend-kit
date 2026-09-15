@echo off
setlocal
set MAVEN_VERSION=3.9.11
set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-%MAVEN_VERSION%
set MAVEN_BIN=%MAVEN_HOME%\bin\mvn.cmd
if not exist "%MAVEN_BIN%" (
  powershell -NoProfile -ExecutionPolicy Bypass -Command "$u='https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip'; $z='%MAVEN_HOME%\maven.zip'; New-Item -ItemType Directory -Force -Path '%MAVEN_HOME%' | Out-Null; Invoke-WebRequest $u -OutFile $z; Expand-Archive $z -DestinationPath '%MAVEN_HOME%' -Force; Move-Item '%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%\*' '%MAVEN_HOME%' -Force; Remove-Item '%MAVEN_HOME%\apache-maven-%MAVEN_VERSION%' -Recurse -Force; Remove-Item $z -Force"
)
call "%MAVEN_BIN%" %*
