cd language
java salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java ./exceptions/*.java
cd ../io
java salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java
cd ../wwc
java salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java
cd ..
javac *.java
