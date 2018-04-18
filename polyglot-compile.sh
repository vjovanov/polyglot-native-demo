#! /bin/bash

ROOT=`pwd`
[ -z ${SCALA_HOME+x} ] && echo "SCALA_HOME must be set to the scala distribution root folder" && exit 1
[ -z ${KOTLIN_HOME+x} ] && echo "KOTLIN_HOME must be set to kotlin distribution root folder" && exit 1
[ -z ${GRAALVM_HOME+x} ] && echo "GRAALVM_HOME must be set to a graalvm distribution root folder" && exit 1

SCALA_CP=$SCALA_HOME/lib/scala-library.jar
KOTLIN_CP=$KOTLIN_HOME/lib/kotlin-stdlib.jar
LIB_CP=$ROOT/lib/jwnl-1.3.3.jar:$ROOT/lib/klaxon-0.30.jar:lib/opennlp-maxent-3.0.3.jar:$ROOT/lib/opennlp-tools-1.5.3.jar
SVM_CP=$GRAALVM_HOME/lib/svm/svm-api.jar
rm -rf target
mkdir target

set -e

echo "Compiling Scala..."
$SCALA_HOME/bin/scalac -cp $SCALA_CP ./scala/src/sentiments/Correlation.scala -d ./target

echo "Compiling Kotlin..."
$KOTLIN_HOME/bin/kotlinc -cp ./target:$LIB_CP ./kotlin/src/sentiments/TweetParser.kt ./kotlin/src/sentiments/PriceParser.kt -d ./target

echo "Compiling Java..."
$GRAALVM_HOME/bin/javac -cp ./target:$SCALA_CP:$KOTLIN_CP:$LIB_CP:$SVM_CP ./java/src/sentiments/* -d ./target

echo "Building the executable..."
$GRAALVM_HOME/bin/native-image --no-server -cp $LIB_CP:$SCALA_CP:./target:$KOTLIN_CP -H:-MultiThreaded -H:Class=sentiments.CInterface -H:Name=sentimentsJava -H:Debug=2 -H:-AOTInline -H:SourceSearchPath=./java/src:./kotlin/src/:./scala/src/

echo "Building the shared library..."
$GRAALVM_HOME/bin/native-image --no-server -cp $LIB_CP:$SCALA_CP:$ROOT/target:$KOTLIN_CP -H:Name=libsentiments -H:SourceSearchPath=./java/src:./kotlin/src/:./scala/src/ -H:Path=./target -H:Debug=2 -H:-AOTInline -H:Kind=SHARED_LIBRARY

echo "Building the native project..."
gcc -g -L./target/ -I./target/ -iquote./target c/main.c -lsentiments -o sentimentsC
