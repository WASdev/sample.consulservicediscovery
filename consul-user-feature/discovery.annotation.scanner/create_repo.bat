@echo off
REM /*******************************************************************************
REM  * Copyright (c) 2015 IBM Corp.
REM  *
REM  * Licensed under the Apache License, Version 2.0 (the "License");
REM  * you may not use this file except in compliance with the License.
REM  * You may obtain a copy of the License at
REM  *
REM  *    http://www.apache.org/licenses/LICENSE-2.0
REM  *
REM  * Unless required by applicable law or agreed to in writing, software
REM  * distributed under the License is distributed on an "AS IS" BASIS,
REM  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
REM  * See the License for the specific language governing permissions and
REM  * limitations under the License.
REM  *******************************************************************************/
REM
REM This batch file takes a local Liberty installation and then creates a Maven repository
REM at the root of this project. This repo is then referenced in the pom.xml.
REM Changes in this file may need to be mirrored in any pom.xml's that use Liberty API/SPI
REM
if "%~1"=="" goto help
SET REPO_HOME=%CD%\repo
echo Adding Liberty APIs/SPIs from %1 to a repo in %REPO_HOME%
echo. 

if exist %REPO_HOME% goto skip_create
echo Repo directory does not exist, creating it
mkdir %REPO_HOME%
echo.
:skip_create

REM these lines are only required if you are creating a local Liberty repository
REM echo Adding Liberty APIs
REM FOR /F "tokens=1,2 delims=_" %%A IN ('dir %1\dev\api\ibm\*.jar /B') DO call :add_to_repo %%A %%B %1\dev\api\ibm\%%A_%%B

REM echo Adding Liberty SPIs
REM FOR /F "tokens=1,2 delims=_" %%A IN ('dir %1\dev\spi\ibm\*.jar /B') DO call :add_to_repo %%A %%B %1\dev\spi\ibm\%%A_%%B

echo Adding Liberty compatibility
FOR /F "tokens=1,2 delims=_" %%A IN ('dir %1\lib\com.ibm.ws.compat_*.jar /B') DO call :add_to_repo %%A %%B %1\lib\%%A_%%B

goto end
:add_to_repo <result> <artifact> <file> <fullpath>
(
	echo Source   : %3
    echo GroupID  : com.ibm.websphere.appserver.api
    echo Artifact : %1
    echo Version  : %~n2
    
   	cmd /C mvn install:install-file -Dfile="%3" -DgroupId=com.ibm.websphere.appserver.api -DartifactId=%1 -Dversion=%~n2 -Dpackaging=jar -DlocalRepositoryPath=%REPO_HOME%

    echo.
 
    exit /b
)

goto end
:help
echo Batch file to populate local lib directory with Liberty APIs and SPIs
echo Usage : create_repo [path to Liberty installation]

:end
