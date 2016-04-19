#!/bin/bash
rm *.java *.class
for f in */
do
   rm $f*.class
   rm $f*.java
done

