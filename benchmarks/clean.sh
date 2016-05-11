#!/bin/bash
rm *.java
rm *.class
for f in */
do
   rm $f*.class
   rm $f*.java
done

