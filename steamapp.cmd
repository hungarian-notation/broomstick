@echo off

REM See end of file for usage, or run with /?
REM 
REM Useful forms:
REM 
REM steamapp -l ID -l DIR 
REM     emits a tab-separated list of all game ids and installation directories
REM 
REM steamapp 892970 -s DIR & call pushd %DIR%
REM     sets the current working directory to the user's Valheim installation

SETLOCAL ENABLEDELAYEDEXPANSION

SET "APPID="
SET "STEAM="
SET "OPT_SET="
SET "OPT_WORKSHOP="
SET "LIBRARY="
SET "OPT_LIST=0"

:HANDLE_ARG
	IF /I "%~1"==""           GOTO MAIN

	SET "arg=%~1"

	IF /I "%~1"=="/?"         GOTO USAGE
	IF /I "%~1"=="/H"         GOTO USAGE
	IF /I "%~1"=="-h"         GOTO USAGE
	IF /I "%~1"=="--help"     GOTO USAGE
	IF /I "%~1"=="/s"         GOTO HANDLE_OPT_SET
	IF /I "%arg:~0,2%"=="-s"  GOTO HANDLE_OPT_SET
	IF /I "%~1"=="--SET"      GOTO HANDLE_OPT_SET
	IF /I "%~1"=="/w"         GOTO HANDLE_OPT_WORKSHOP
	IF /I "%~1"=="-w"         GOTO HANDLE_OPT_WORKSHOP
	IF /I "%~1"=="--workshop" GOTO HANDLE_OPT_WORKSHOP
	IF /I "%~1"=="/l"         GOTO HANDLE_OPT_LIST
	IF /I "%arg:~0,2%"=="-l"  GOTO HANDLE_OPT_LIST
	IF /I "%~1"=="--list"     GOTO HANDLE_OPT_LIST

	IF "%arg:~0,1%"=="/"      GOTO FAILURE_UNKNOWN_ARGUMENT
	IF "%arg:~0,1%"=="-"      GOTO FAILURE_UNKNOWN_ARGUMENT
	IF NOT DEFINED APPID (
		SET "APPID=%~1"
	) else (
		GOTO FAILURE_UNKNOWN_ARGUMENT
	)
	SHIFT /1 && GOTO HANDLE_ARG
	:HANDLE_OPT_SET
		IF /I "%arg:~0,2%"=="-s" IF /I NOT "%arg%"=="-s" (			
			SET "OPT_SET=%arg:~2%"
			SHIFT /1
		) ELSE (
			SET "OPT_SET=%~2"
			SHIFT /1 
			SHIFT /1
		)
		GOTO HANDLE_ARG
	:HANDLE_OPT_WORKSHOP
		SET "OPT_WORKSHOP=1"
		SHIFT /1
		GOTO HANDLE_ARG
	:HANDLE_OPT_LIST
		IF /I "%arg:~0,2%"=="-l" IF /I NOT "%arg%"=="-l" (			
			SET /A OPT_LIST+=1
			SET "OPT_LIST[!OPT_LIST!]=%arg:~2%"
			SHIFT /1 
		) ELSE (
			SET /A OPT_LIST+=1
			SET "OPT_LIST[!OPT_LIST!]=%~2"
			SHIFT /1 
			SHIFT /1
		)
		GOTO HANDLE_ARG
:MAIN
	IF "%OPT_LIST%"=="0" IF NOT DEFINED APPID GOTO FAILURE_ARGUMENTS
	SET "STEAM="
	SET "RESULT="

	FOR /F "tokens=2,*" %%A IN ('REG QUERY "HKEY_LOCAL_MACHINE\SOFTWARE\WOW6432Node\Valve\Steam" /V "InstallPath" 2^>nul') DO SET "STEAM=%%B"
	IF NOT DEFINED STEAM GOTO FAILURE_NOSTEAM

	SET DEPTH=0
	FOR /F "usebackq tokens=*" %%A IN ("%STEAM%\steamapps\libraryfolders.vdf") DO (
		SET "LINE=%%A"

		FOR /F "tokens=1*" %%I IN ("%%A") DO (
			SET "LKY=!KEY!"
			SET "KEY=%%~I"
			SET "VAL=%%~J"
		)

		IF "!KEY!"=="{" (
			SET /A DEPTH+=1
			SET "TBL[!DEPTH!]=!LKY!"
			FOR %%D IN (!DEPTH!) DO SET "TBL=!TBL[%%D]!"
		) ELSE IF "!KEY!"=="}" (
			SET "TBL[!DEPTH!]="
			SET /A DEPTH-=1
			FOR %%D IN (!DEPTH!) DO SET "TBL=!TBL[%%D]!"
		) ELSE (			
			REM Track the current library path in %LIBRARY%
			IF "!DEPTH!"=="2" IF "!KEY!"=="path" SET "LIBRARY=!VAL:\\=\!"
			REM Match our target APPID

			IF "!DEPTH!"=="3" IF "!TBL!"=="apps" (
				IF NOT "%OPT_LIST%"=="0" (					
					REM ==== PRINT LISTING ====
					SET "APPMANIFEST=!LIBRARY!\steamapps\appmanifest_!KEY!.acf"
					CALL :QUERY_VDF "!APPMANIFEST!" "/AppState/installdir" INSTALLDIRNAME "/AppState/name" NAME
					SET "INSTALLDIR=!LIBRARY!\steamapps\common\!INSTALLDIRNAME!"
					SET "LISTING="
					FOR /L %%F IN (1,1,!OPT_LIST!) DO (
						SET FIELD=!OPT_LIST[%%F]!
						SET FIELDVAL=
						IF /I "!FIELD!"=="ID" ( 
							SET "FIELDVAL=!KEY!"
						) ELSE IF /I "!FIELD!"=="NAME" ( 
							SET "FIELDVAL=!NAME!"
						) ELSE IF /I "!FIELD!"=="DIRNAME" (
							SET "FIELDVAL=!INSTALLDIRNAME!"
						) ELSE IF /I "!FIELD!"=="DIR" ( 
							SET "FIELDVAL=!INSTALLDIR!"
						) ELSE IF /I "!FIELD!"=="WORKSHOP" (
							SET "WORKSHOP=!LIBRARY!\steamapps\workshop\content\!KEY!"					
							IF NOT EXIST "!WORKSHOP!" SET "WORKSHOP="
							SET "FIELDVAL=!WORKSHOP!"
						) ELSE (
							ECHO ERROR: unknown list field: "!FIELD!" 1>&2
							EXIT /B 1
						)
						IF "!LISTING!"=="" (
							SET LISTING="!FIELDVAL!"
						) ELSE (
							SET LISTING=!LISTING!	"!FIELDVAL!"
						)
					)
					ECHO !LISTING!
				) ELSE IF "!KEY!"=="%APPID%" (
					GOTO found
				)
			)
		)
	)

	IF NOT "%OPT_LIST%"=="0" GOTO :EOF

	REM We fell out of the loop searching for the APPID, it's not there.
	GOTO FAILURE_APPID

	:FOUND
		SET "LIBRARY=!LIBRARY!"
		SET "APPMANIFEST=%LIBRARY%\steamapps\appmanifest_%APPID%.acf"
		SET APPNAME=
		SET INSTALLDIR=
		CALL :QUERY_VDF "%APPMANIFEST%" "/AppState/name" APPNAME "/AppState/installdir" INSTALLDIR
		IF DEFINED OPT_WORKSHOP GOTO RETURN_WORKSHOP
		:RETURN_INSTALLDIR
			IF ERRORLEVEL 1 EXIT /B 1
			SET "RESULT=%LIBRARY%\steamapps\common\%INSTALLDIR%"
			IF EXIST "%RESULT%" GOTO DONE
			GOTO FAILURE_NO_INSTALLDIR
		:RETURN_WORKSHOP
			SET "RESULT=%LIBRARY%\steamapps\workshop\content\%APPID%"
			IF EXIST "%RESULT%" GOTO DONE
			GOTO FAILURE_NO_WORKSHOP
	:DONE
		IF DEFINED OPT_SET (
			ENDLOCAL && SET "%OPT_SET%=%RESULT%"
			GOTO :EOF
		) else (
			ECHO !RESULT!
			GOTO :EOF
		)

:QUERY_VDF
	REM QUERY_VDF <file> <keypath> <outvar> [<keypath2> <outvar2> [<keypath3> <outvar3>]]
	REM retrieves the key specified by keypath
	REM for example, /x/y/z refers to "x" { "y" { "z" "this value" } }
	SETLOCAL

	IF NOT EXIST "%~1" (
		ECHO ERROR: "%~1" does not exist 1>&2
		EXIT /B 1
	)

	SET "DEPTH=0"
	SET "R1="
	SET "R2="
	SET "R3="
	FOR /F "usebackq tokens=*" %%A IN ("%~1") DO (
		SET "LINE=%%A"

		FOR /F "tokens=1*" %%I IN ("%%A") DO (
			SET "LKY=!KEY!"
			SET "KEY=%%~I"
			SET "VAL=%%~J"
		)

		IF "!KEY!"=="{" (
			SET /A DEPTH+=1
			SET "KEYPATH[!DEPTH!]=!LKY!"
		) ELSE IF "!KEY!"=="}" (
			SET "KEYPATH[!DEPTH!]="
			SET /A DEPTH-=1
		) ELSE (
			SET KEYPATH=
			FOR /L %%D IN (1,1,!DEPTH!) DO SET "KEYPATH=!KEYPATH!/!KEYPATH[%%D]!"
			IF "!KEYPATH!/!KEY!"=="%~2" SET "R1=!VAL!"
			IF "!KEYPATH!/!KEY!"=="%~4" SET "R2=!VAL!"
			IF "!KEYPATH!/!KEY!"=="%~6" SET "R3=!VAL!"
			IF NOT "%~7"=="" (
				IF DEFINED R1 IF DEFINED R2 IF DEFINED R3 GOTO QUERY_VDF_DONE
			) ELSE IF NOT "%~5"=="" (
				IF DEFINED R1 IF DEFINED R2 GOTO QUERY_VDF_DONE
			) ELSE (
				IF DEFINED R1 GOTO QUERY_VDF_DONE
			)
		)
	)

	:QUERY_VDF_DONE

	IF NOT "%~7"=="" (
		ENDLOCAL && SET "%~3=%R1%" && SET "%~5=%R2%" && SET "%~7=%R3%"
	) ELSE IF NOT "%~5"=="" (
		ENDLOCAL && SET "%~3=%R1%" && SET "%~5=%R2%"
	) ELSE (
		ENDLOCAL && SET "%~3=%R1%"
	)

	GOTO :EOF

:FAILURE_UNKNOWN_ARGUMENT
	ECHO ERROR: unexpected argument: "%~1" 1>&2
	EXIT /B 1

:FAILURE_ARGUMENTS
	ECHO ERROR: not enough arguments 1>&2
	CALL :USAGE 1>&2
	EXIT /B 1

:FAILURE_NO_WORKSHOP
	ECHO ERROR: found library for %APPNAME%, but "%RESULT%" does not exist 1>&2
	EXIT /B 1

:FAILURE_NO_INSTALLDIR
	ECHO ERROR: found library for %APPNAME%, but "%RESULT%" does not exist 1>&2
	EXIT /B 1

:FAILURE_NOSTEAM
	ECHO ERROR: Could not find steam installation 1>&2
	EXIT /B 1

:FAILURE_APPID
	ECHO ERROR: "%APPID%" is not installed 1>&2
	EXIT /B 1

:USAGE
	IF "%~0"==":USAGE" SHIFT
	ECHO.
	ECHO usage: %~0 [-w] [-s VAR] APPID
	ECHO        %~0 --list FIELD [ --list FIELD [ --list FIELD ] ... ]
	ECHO.
	ECHO     Locates the installation folder for the steam app with the given appid.
	ECHO.
	ECHO OPTIONS
	ECHO     -h --help        prints this message
	ECHO     -w --workshop    find the workshop content directory, rather than the installation
	ECHO     -s --set VAR     store the result path in the environment variable VAR
	ECHO     -l --list FIELD  List the given field for all games. Can be repeated to add columns
	ECHO                        to the output.
	ECHO.
	ECHO FIELDS
	ECHO     ID       The application's ID
	ECHO     NAME     The application's name in a display format.
	ECHO     DIRNAME  The name of the application's installation directory.
	ECHO     DIR      The full path of the application's installation directory.
	ECHO     WORKSHOP The full path of the application's workshop directory, or an empty pair 
	ECHO              of quotes if the game has no workshop directory.
	ECHO.             
	GOTO :EOF