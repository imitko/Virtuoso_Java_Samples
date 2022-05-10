@echo off
REM
REM  JBench - a JDBC Benchmark program
REM  Copyright (C) 1999-2022 OpenLink Software.
REM
REM  This program is free software; you can redistribute it and/or modify
REM  it under the terms of the GNU General Public License as published by
REM  the Free Software Foundation; either version 2 of the License, or
REM  (at your option) any later version.
REM
REM  This program is distributed in the hope that it will be useful,
REM  but WITHOUT ANY WARRANTY; without even the implied warranty of
REM  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
REM  GNU General Public License for more details.
REM
REM  You should have received a copy of the GNU General Public License
REM  along with this program; if not, write to the Free Software
REM  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA


echo -------------------------------------------------------------------------- 
echo TPC-Like Benchmark Program for JDBC Drivers and Databases 
echo from OpenLink Software 
echo -------------------------------------------------------------------------- 

set CLASSPATH=./virtjdbc4_2.jar;./jbench.jar
java -Djdbc.drivers=virtuoso.jdbc4.Driver BenchMain "URL=jdbc:virtuoso://localhost:1111/UID=dba/PWD=dba"  "DRIVERTYPE=0"
