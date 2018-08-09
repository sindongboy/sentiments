#!/bin/bash

# run script template

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo "usage: $0 [options]"
	echo "-h	help"
	echo "-c	category number"
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

if [[ -z ${category} ]]; then
	usage
fi

# --------------- #
# Environments 
# --------------- #
# env.
CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-trunk/config"
NLP_CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterms/config"
NLP_DICT="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterms/resource"
DICTIONARY="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-resources/resource/sentiments"
SYSDICT="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-resources/resource/sys"

# dependency
DEP=`find ../lib -type f -name "*" | awk '{printf("%s:", $0);}' | sed 's/:$//g'`
TARGET="../target/sentimentAnalyzer-core-1.3.9-SNAPSHOT.jar"
CP="${CONFIG}:${NLP_CONFIG}:${NLP_DICT}:${DICTIONARY}:${SYSDICT}:${DEP}:${TARGET}"


# --------------- #
# Business Logic
# --------------- #

# make sure ${JAVA_HOME} set properly
java -Xmx8G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.SentimentCLI -c ${category}
