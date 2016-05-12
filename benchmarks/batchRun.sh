#!/bin/bash



java -Dnstages=1 BenchmarkRunner Big 20000 120 
java -Dnstages=1 BenchmarkRunner Big 25000 120 
java -Dnstages=1 BenchmarkRunner Big 30000 120 
java -Dnstages=1 BenchmarkRunner Big 35000 120 
java -Dnstages=1 BenchmarkRunner Big 40000 120 
java -Dnstages=1 BenchmarkRunner Big 45000 120 

java -Dnstages=2 BenchmarkRunner Big 20000 120 
java -Dnstages=2 BenchmarkRunner Big 25000 120 
java -Dnstages=2 BenchmarkRunner Big 30000 120 
java -Dnstages=2 BenchmarkRunner Big 35000 120 
java -Dnstages=2 BenchmarkRunner Big 40000 120 
java -Dnstages=2 BenchmarkRunner Big 45000 120 

java -Dnstages=4 BenchmarkRunner Big 20000 120 
java -Dnstages=4 BenchmarkRunner Big 25000 120 
java -Dnstages=4 BenchmarkRunner Big 30000 120 
java -Dnstages=4 BenchmarkRunner Big 35000 120 
java -Dnstages=4 BenchmarkRunner Big 40000 120 
java -Dnstages=4 BenchmarkRunner Big 45000 120 

java -Dnstages=8 BenchmarkRunner Big 20000 120 
java -Dnstages=8 BenchmarkRunner Big 25000 120 
java -Dnstages=8 BenchmarkRunner Big 30000 120 
java -Dnstages=8 BenchmarkRunner Big 35000 120 
java -Dnstages=8 BenchmarkRunner Big 40000 120 
java -Dnstages=8 BenchmarkRunner Big 45000 120 

java -Dnstages=16 BenchmarkRunner Big 20000 120 
java -Dnstages=16 BenchmarkRunner Big 25000 120 
java -Dnstages=16 BenchmarkRunner Big 30000 120 
java -Dnstages=16 BenchmarkRunner Big 35000 120 
java -Dnstages=16 BenchmarkRunner Big 40000 120 
java -Dnstages=16 BenchmarkRunner Big 45000 120 


