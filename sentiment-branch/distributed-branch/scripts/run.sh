#!/bin/bash

JAR="../target/sentimentAnalyzer-core-1.5.0-SNAPSHOT.jar"
MAIN="com.skplanet.nlp.sentiment.dist.WordCount"

#hadoop jar sentimentAnalyzer-core-1.5.0-SNAPSHOT.jar com.skplanet.nlp.sentiment.dist.WordCount /dmp/tas/data/product/input/review.tsv /dmp/tas/data/product/output/
hadoop fs -rmdir /dmp/tas/data/product/output
hadoop jar ${JAR} com.skplanet.nlp.sentiment.dist.WordCount /dmp/tas/data/product/input/review.tsv /dmp/tas/data/product/output/
hadoop fs -getmerge /dmp/tas/data/product/output/ ./result.out

