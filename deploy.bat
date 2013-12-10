::REM set ANT_HOME to the location where you unpacked Ant.  Inside this
::REM directory should be bin and lib directories containing the Ant
::REM executables and library dependencies.
::REM set JAVA_HOME to the location of the installed JDK (customize as necessary)
REM This script will not work unless you make sure that ANT_HOME, M2_HOME, and JAVA_HOME are pointing at the correct location.
set ANT_HOME=C:\JavaLibraries\apache-ant-1.9.2
set M2_HOME=C:\JavaLibraries\apache-maven-3.1.1
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_45
set PATH=%M2_HOME%\bin;%ANT_HOME%\bin;%JAVA_HOME%\bin;%PATH%
ant deploy server
