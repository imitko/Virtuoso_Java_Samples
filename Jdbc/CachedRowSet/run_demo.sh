#!/bin/sh
#

echo -------------------------------------------------------------------------- 
echo CachedRowSet demo from OpenLink Software 
echo -------------------------------------------------------------------------- 

export CLASSPATH=./virtjdbc4_2.jar:./CachedRowSet.jar:$CLASSPATH
java CachedRowSet

