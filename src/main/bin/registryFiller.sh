#!/bin/sh

LIB=../share
CLASSPATH=.:./log4j.properties:
JAVA=java

for f in `ls $LIB`
do
	CLASSPATH=$CLASSPATH:$LIB/$f
done

echo $CLASSPATH

$JAVA -Xmx1024M -cp $CLASSPATH clarin.cmdi.componentregistry.tools.RegistryFiller $*