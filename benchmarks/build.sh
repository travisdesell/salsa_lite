#!/bin/bash
java salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java

for f in */
do
   java salsa_lite.compiler.SalsaCompiler $f/*.salsa
   javac $f/*.java
done

