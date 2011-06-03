@echo off
setlocal

set SBT_VER=0.7.5
set SBT_DIR=%~dp0project\boot\scala-2.7.7\org.scala-tools.sbt\sbt\%SBT_VER%
set SBT_JAR=%SBT_DIR%\sbt-launch-%SBT_VER%.jar

if exist "%SBT_JAR%" goto :SBT_JAR_OK

rem #################################################################################
rem ### This portion will download the SBT launch from the Google Code repository ###
rem #################################################################################

set SLD_CLS=SBTLaunchDownloader
set SLD_JAR=%SBT_DIR%\sbt-launch-%SBT_VER%-downloader.jar

if exist "%SLD_JAR%" goto :SDL_JAR_OK

if exist "%SBT_DIR%" goto :SBT_DIR_OK
mkdir "%SBT_DIR%"
if not exist "%SBT_DIR%" goto :SBT_DIR_ERR

:SBT_DIR_OK

set SLD_SRC=%SBT_DIR%\%SLD_CLS%.java
set SLD_BIN=%SBT_DIR%\org\xsbt\%SLD_CLS%.class

> "%SLD_SRC%" echo package org.xsbt;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo import java.io.*;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo public class %SLD_CLS%
>>"%SLD_SRC%" echo {
>>"%SLD_SRC%" echo   //  List of supported SBT launch versions, with sizes and SHA-1 checksums
>>"%SLD_SRC%" echo   private static enum SBTLaunchJar {
>>"%SLD_SRC%" echo     SL070RELEASE("0.7.0", "2.7.7", 893629, "bb17fa11f4b754b2245981b566f2fe113384aca7"),
>>"%SLD_SRC%" echo     SL071RELEASE("0.7.1", "2.7.7", 893630, "c9ed8325256bb143ff74e851a8279c11141778db"),
>>"%SLD_SRC%" echo     SL072RELEASE("0.7.2", "2.7.7", 906327, "dda70742494139b65766d29ebdf718b326f67b84"),
>>"%SLD_SRC%" echo     SL073RELEASE("0.7.3", "2.7.7", 920993, "45c5b013a371bfbe60ce467ecdb49411897d7666"),
>>"%SLD_SRC%" echo     SL074RELEASE("0.7.4", "2.7.7", 928236, "2b7cfadf05b3b26285bb2038145479741268d334"),
>>"%SLD_SRC%" echo     SL075RELEASE("0.7.5", "2.7.7", 948813, "b398555a69ef9317f7840020fa57bd8f23eaf60e"),
>>"%SLD_SRC%" echo     SL076RC0("0.7.6.RC0", "2.7.7", 952178, "7dccd4928d4792df0b460e50bd61192770e2653f");
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     public final String version;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     public final String jarFileName;
>>"%SLD_SRC%" echo     public final String jarPath;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     public final String downloadURL;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     public final int size;
>>"%SLD_SRC%" echo     public final String sha1;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     private SBTLaunchJar(final String version, final String scalaVersion, final int size, final String sha1) {
>>"%SLD_SRC%" echo       this.version = version;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       this.jarPath = "project/boot/scala-"+ scalaVersion +"/org.scala-tools.sbt/sbt/"+ version;
>>"%SLD_SRC%" echo       this.jarFileName = "sbt-launch-"+ version +".jar";
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       this.downloadURL = "http://simple-build-tool.googlecode.com/files/" + jarFileName;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       this.size = size;
>>"%SLD_SRC%" echo       this.sha1 = sha1;
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     public static SBTLaunchJar findJar(final String fileName) {
>>"%SLD_SRC%" echo       for(final SBTLaunchJar cur: values())
>>"%SLD_SRC%" echo         if (fileName.equals(cur.version)) return cur;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       return null;
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   protected final SBTLaunchJar sbtJar;
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   public SBTLaunchDownloader(final SBTLaunchJar sbtJar) throws Throwable {
>>"%SLD_SRC%" echo     this.sbtJar = sbtJar;
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   public byte[] download() throws IOException {
>>"%SLD_SRC%" echo     final java.net.URL sbtJarURL = new java.net.URL(sbtJar.downloadURL);
>>"%SLD_SRC%" echo     final InputStream iS = new BufferedInputStream(sbtJarURL.openStream());
>>"%SLD_SRC%" echo     final ByteArrayOutputStream bAOS = new ByteArrayOutputStream(sbtJar.size);
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     try{
>>"%SLD_SRC%" echo       while(true) {
>>"%SLD_SRC%" echo         // Already buffered via BufferedInputStream, no need for read via byte array
>>"%SLD_SRC%" echo         final int next = iS.read();
>>"%SLD_SRC%" echo         if (next == -1) break;
>>"%SLD_SRC%" echo         bAOS.write(next);
>>"%SLD_SRC%" echo       }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       return bAOS.toByteArray();
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo     finally {
>>"%SLD_SRC%" echo       iS.close();
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   public void check(final byte[] body) throws Throwable {
>>"%SLD_SRC%" echo     if (body.length != sbtJar.size)
>>"%SLD_SRC%" echo       throw new IOException("Downloaded jar byte size does not match! (expected "+ sbtJar.size +", got "+ body.length +" bytes)");
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     final byte[] digest = java.security.MessageDigest.getInstance("SHA-1").digest(body);
>>"%SLD_SRC%" echo     final String hash = javax.xml.bind.DatatypeConverter.printHexBinary(digest);
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     if (!hash.equalsIgnoreCase(sbtJar.sha1))
>>"%SLD_SRC%" echo       throw new SecurityException("Downloaded jar SHA-1 checksum does not match! (expected "+ sbtJar.sha1 +", got "+ hash +")");
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   public void write(final byte[] body) throws Throwable {
>>"%SLD_SRC%" echo     final File sbtJarFile = new File(sbtJar.jarPath, sbtJar.jarFileName);
>>"%SLD_SRC%" echo     final OutputStream oS = new FileOutputStream(sbtJarFile);
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     try{
>>"%SLD_SRC%" echo       oS.write(body);
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo     finally {
>>"%SLD_SRC%" echo       oS.close();
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       // Ensure that no incomplete write occurred (disk full, ...)
>>"%SLD_SRC%" echo       if (sbtJarFile.length() != sbtJar.size)
>>"%SLD_SRC%" echo         sbtJarFile.delete();
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   public void performDownload() throws Throwable {
>>"%SLD_SRC%" echo     final byte[] body = download();
>>"%SLD_SRC%" echo     check(body);
>>"%SLD_SRC%" echo     write(body);
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo   public static void main(final String args[]) {
>>"%SLD_SRC%" echo     try {
>>"%SLD_SRC%" echo       if (args.length != 1)
>>"%SLD_SRC%" echo         throw new IllegalArgumentException("Illegal number of arguments!");
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       final SBTLaunchJar sbtJar = SBTLaunchJar.findJar(args[0]);
>>"%SLD_SRC%" echo       if (sbtJar == null)
>>"%SLD_SRC%" echo         throw new IllegalArgumentException("SBT launch version not supported! ("+ args[0] +")");
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo       new SBTLaunchDownloader(sbtJar).performDownload();
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo     catch(final Throwable t) {
>>"%SLD_SRC%" echo       System.err.println(t);
>>"%SLD_SRC%" echo       System.exit(-1);
>>"%SLD_SRC%" echo     }
>>"%SLD_SRC%" echo.
>>"%SLD_SRC%" echo     System.exit(0);
>>"%SLD_SRC%" echo   }
>>"%SLD_SRC%" echo }

if not exist "%SLD_SRC%" goto :SLD_SRC_ERR

javac.exe -d "%SBT_DIR%" "%SLD_SRC%"
del "%SLD_SRC%"

if not exist "%SLD_BIN%" goto :SLD_BIN_ERR

jar.exe cfe "%SLD_JAR%" org.xsbt.%SLD_CLS% -C "%SBT_DIR%" org
rmdir /S /Q "%SBT_DIR%\org"

if not exist "%SLD_JAR%" goto :SLD_JAR_ERR

:SDL_JAR_OK

echo Downloading the SBT launch...
java.exe -jar "%SLD_JAR%" "%SBT_VER%"

if not exist "%SBT_JAR%" goto :SBT_JAR_ERR

rem ####################################################################################
rem ### We have located the SBT launch jar, now we need to process the parameters    ###
rem ### JVM and SBT parameters that get sent by default need to be specified below   ###
rem ####################################################################################

:SBT_JAR_OK

set JVM_PARAMS=-Xss4m -Xmx1g -Xms256m -XX:MaxPermSize=256m
set SBT_PARAMS=
set RUN_IN_LOOP=

:PARSER_LOOP
if "%~1"=="" goto PARSER_END
call :PROCESS_PARAM "%~1"
shift
goto PARSER_LOOP
:PARSER_END

echo Invoking SBT:
echo JVM params: %JVM_PARAMS%
echo SBT params: %SBT_PARAMS%
echo.

:SBT_LOOP
java.exe %JVM_PARAMS% -jar %SBT_JAR% %SBT_PARAMS%
if "%RUN_IN_LOOP%"=="true" goto :SBT_LOOP

goto :END

rem ###############################################################
rem ### Switch for any custom params that need to be translated ###
rem ###############################################################

:PROCESS_PARAM

if "%~1"=="--kill" (
  taskkill /f /im java.exe
  goto :EOF
)

if "%~1"=="--jrebel" (
  if exist "%JREBEL_HOME%\jrebel.jar" set JVM_PARAMS=%JVM_PARAMS% -noverify -javaagent:%JREBEL_HOME%\jrebel.jar -XX:+CMSClassUnloadingEnabled
  goto :EOF
)

if "%~1"=="--loop" (
  set RUN_IN_LOOP=true
  goto :EOF
)

if "%~1"=="~lift" (
  set JVM_PARAMS=%JVM_PARAMS% -Drebel.lift_plugin=true
  set SBT_PARAMS=%SBT_PARAMS% jetty-run ~prepare-webapp jetty-stop
  goto :EOF
)

set SBT_PARAMS=%SBT_PARAMS% %1
goto :EOF

  rem ######################
rem ### Error messages ###
rem ######################

:SBT_DIR_ERR
echo Could not create SBT managed directory!
goto :END

:SLD_SRC_ERR
echo Could not create the SBT launch downloader source file!
goto :END

:SLD_BIN_ERR
echo Could not compile the SBT launch downloader!
goto :END

:SLD_JAR_ERR
echo Could not create the SBT launch downloader jar!
goto :END

:SBT_JAR_ERR
echo Could not download the SBT launch!
goto :END

:END
endlocal
