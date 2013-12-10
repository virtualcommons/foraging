REM set M2_HOME to the location where you unpacked Maven.  Inside this
REM directory should be bin and lib directories containing the Maven
REM executables and library dependencies.
REM *******
REM You MUST RUN deploy.bat or server.bat before this to properly compile and
REM configure this application.
set ANT_HOME=C:\JavaLibraries\apache-ant-1.9.2
set M2_HOME=C:\JavaLibraries\apache-maven-3.1.1
set JAVA_HOME=C:\Program Files\Java\jdk1.7.0_45
set PATH=%JAVA_HOME%\bin;%ANT_HOME%\bin;%M2_HOME%\bin;%PATH%
mvn jetty:run
