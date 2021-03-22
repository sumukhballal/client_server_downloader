directory="./src/main/"
test=./src/test/
output=./target/
junit=./lib/junit-platform-console-standalone-1.8.0-M1.jar


compile:
	javac ${directory}*.java -d ${output}
	javac -cp ${output};${junit} ${test}*.java -d ${output}
run:
	java -cp .\target main.Client

testTransferOneFileTest:
	java -jar ${junit} -cp .\target  --select-class test.EvaluationTests -m test.EvaluationTests#transferOneFileTest()

testScaleToFourTest:
	java -jar ${junit} -cp .\target  --select-class test.EvaluationTests -m test.EvaluationTests#scaleToFourTest()

testTransferSpeedClientConcurrent:
	java -jar ${junit} -cp .\target  --select-class test.EvaluationTests -m test.EvaluationTests#transferSpeedClientConcurrent()

testTransferSpeedFileSizeConcurrent:
	java -jar ${junit} -cp .\target  --select-class test.EvaluationTests -m test.EvaluationTests#transferSpeedFileSizeConcurrent()

testCreateFiles:
	java -jar ${junit} -cp .\target  --select-class test.EvaluationTests -m test.EvaluationTests#createFileTest()
