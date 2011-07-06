cd io
java -Dwwc salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java
cd ../language
java -Dwwc salsa_lite.compiler.SalsaCompiler *.salsa
javac *.java ./exceptions/*.java
cd ..
javac *.java
