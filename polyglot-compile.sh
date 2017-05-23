#/bin/bash -x

ROOT=`pwd`
SCALA_CP=$SCALA_HOME/lib/scala-library.jar
KOTLIN_CP=$KOTLIN_HOME/lib/kotlin-stdlib.jar
LIB_CP=$ROOT/lib/jwnl-1.3.3.jar:$ROOT/lib/klaxon-0.30.jar:lib/opennlp-maxent-3.0.3.jar:$ROOT/lib/opennlp-tools-1.5.3.jar
SVM_CP=$GRAALVM_DIR/lib/svm/svm-api.jar
rm -rf target
mkdir target

set -e

scalac -cp $SCALA_CP ./scala/src/sentiments/Correlation.scala -d ./target

kotlinc -cp ./target:$LIB_CP ./kotlin/src/sentiments/TweetParser.kt ./kotlin/src/sentiments/PriceParser.kt -d ./target

javac -cp ./target:$SCALA_CP:$KOTLIN_CP:$LIB_CP:$SVM_CP ./java/src/sentiments/* -d ./target

$GRAALVM_DIR/bin/aot-image -cp $LIB_CP:$SCALA_CP:./target:$KOTLIN_CP -H:-MultiThreaded -H:Class=sentiments.CInterface -H:Name=sentiments -H:+Debug -H:-AOTInline -H:+ReportDeletedElementsAtRuntime -H:SourceSearchPath=./java/src:./kotlin/src/:./scala/src/

#$GRAALVM_DIR/bin/aot-image -cp $LIB_CP:$SCALA_CP:$ROOT/target:$KOTLIN_CP -H:Name=libsentiments -H:SourceSearchPath=./java/src:./kotlin/src/:./scala/src/ -H:Path=./c -H:Kind=SHARED_LIBRARY

