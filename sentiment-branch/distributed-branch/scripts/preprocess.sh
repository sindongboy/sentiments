#!/bin/bash

# run script template

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo "usage: $0 [options]"
	echo "-h	help"
	echo "-c	category"
	exit 1
}

# --------------- #
# Interface 
# --------------- #

while test $# -gt 0; do
	case "$1" in
		-h)
			shift
			usage
			;;
		-c)
			shift
			category=$1
			shift ;;
		*)
			break
			;;
	esac
done	

# --------------- #
# Environments 
# --------------- #
# env.
CONFIG="../config"
NLP_CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterm/trunk/config"
NLP_DICT="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterm/trunk/resource"
DICTIONARY="/Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sentiments"
SYSDICT="/Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sys"

# Target 
TARGET="../target/sentimentAnalyzer-core-1.5.0-SNAPSHOT.jar"
# dependency
CP=`find ../lib -type f -name "*.jar" | awk '{printf("%s:", $0);}'`
CP="${CP}:${CONFIG}:${NLP_CONFIG}:${NLP_DICT}:${DICTIONARY}:${SYSDICT}:${TARGET}"

# --------------- #
# Business Logic
# --------------- #

java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.DictPreprocessor -c ${category}
