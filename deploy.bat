REM set ANT_HOME to the location where you unpacked Ant.  Inside this
REM directory should be bin and lib directories containing the Ant
REM executables and library dependencies.
REM set JAVA_HOME to the location of the installed JDK (customize as necessary)
set ANT_HOME=C:\tools\ant
set M2_HOME=C:\tools\maven
set JAVA_HOME=C:\Program Files\Java\jdk1.6.0_26
set PATH=%M2_HOME%\bin;%ANT_HOME%\bin;%JAVA_HOME%\bin;%PATH%
mvn clean compile & ant clean deploy server
