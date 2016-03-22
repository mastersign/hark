@ECHO OFF
Setlocal EnableDelayedExpansion

SET RELEASE_DIR=release
SET STAGING=%RELEASE_DIR%\staging

PUSHD "%~dp0.."

:PREPARE

IF EXIST %STAGING%\ (
    RMDIR /S /Q %STAGING%
)
MKDIR %STAGING%\bin

:BUILD

CALL lein deps
CALL lein jar
CALL "%~dp0\build-docs.cmd"

:ANALYZE

FOR %%f IN (target\hark-*.jar) DO (
	SET RELEASE_NAME=%%~nf
	SET VERSION=!RELEASE_NAME:hark-=!
	ECHO Version: "!VERSION!"
	GOTO:STAGE
)

:STAGE

XCOPY "target\hark-!VERSION!.jar" %STAGING%\bin\

XCOPY /S doc %STAGING%\doc\

XCOPY /S src %STAGING%\src\
COPY project.clj %STAGING%\
COPY README.md %STAGING%\
COPY LICENSE.md %STAGING%\
COPY CHANGELOG.md %STAGING%\

:COMPRESS

SET ARCHIVE=%RELEASE_DIR%\hark-!VERSION!.zip
IF EXIST "!ARCHIVE!" DEL "!ARCHIVE!"
PUSHD %STAGING%
7z a "%~dp0..\!ARCHIVE!" *
POPD

POPD
