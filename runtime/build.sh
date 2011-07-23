cd language
java -Dlocal_fcs salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java ./exceptions/*.java
cd ../io
java -Dlocal_fcs salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java
cd ..
javac *.java
