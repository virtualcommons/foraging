REM set ANT_HOME to the location where you unpacked Ant.  Inside this
REM directory should be bin and lib directories containing the Ant
REM executables and library dependencies.
set ANT_HOME=C:\tools\ant
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_26
set PATH=%ANT_HOME%\bin;%JAVA_HOME%\bin;%PATH%
ant deploy
