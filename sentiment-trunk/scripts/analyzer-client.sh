#!/bin/bash

# run script template

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo "usage: $0 [options]"
	echo "-h	help"
	echo "-p	port"
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
		-p)
			shift
			port=$1
			shift ;;
		*)
			break
			;;
	esac
done	

if [[ -z ${port} ]]; then
	usage
fi

# --------------- #
# Environments 
# --------------- #
# env.
CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-trunk/config"
NLP_CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterm/trunk/config"
NLP_DICT="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterm/trunk/resource"
DICTIONARY="/Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sentiments"
SYSDICT="/Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sys"

# dependency
DEP=`find ../lib -type f -name "*" | awk '{printf("%s:", $0);}' | sed 's/:$//g'`
TARGET="../target/sentimentAnalyzer-core-1.3.9.jar"

# --------------- #
# Business Logic
# --------------- #

# make sure ${JAVA_HOME} set properly
java -Xmx4G -Dfile.encoding=UTF-8 -cp ${DEP}:${TARGET}:${CONFIG}:${NLP_CONFIG}:${NLP_DICT}:${DICTIONARY}:${SYSDICT} com.skplanet.nlp.sentiment.driver.SentimentClient -p ${port}
