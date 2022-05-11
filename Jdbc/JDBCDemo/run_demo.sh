#!/bin/sh
#

echo -------------------------------------------------------------------------- 
echo JDBCDemo from OpenLink Software 
echo -------------------------------------------------------------------------- 

export CLASSPATH=./virtjdbc4_2.jar:./JDBCDemo.jar:$CLASSPATH
java JDBCDemo

