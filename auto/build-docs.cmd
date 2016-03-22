@ECHO OFF

PUSHD "%~dp0.."
CALL lein codox
CALL javadoc -d doc\java -sourcepath src\java -link http://docs.oracle.com/javase/7/docs/api/ net.kiertscher.io.hark
POPD
