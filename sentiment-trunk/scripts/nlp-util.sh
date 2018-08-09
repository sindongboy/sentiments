#!/bin/bash

# ==================== #
#  NLP Utility Tester  #
# ==================== #

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo "usage: $0 [options]"
	echo "-h	help"
	echo "-c	command line mode"
	echo "-f	single file mode, -f /path/to/the/file"
	echo "-d	multiple files mode, -d /path/to/the/directory"
	echo "-o	output file, -o /path/to/the/output"
	exit 1
}

# --------------- #
# Interface 
# --------------- #

MODE=""
while test $# -gt 0; do
	case "$1" in
		-h)
			shift
			usage
			;;
		-c)
			shift
			MODE="CLI"
			;;
		-f)
			shift
			MODE="FILE"
			INPUT=$1
			shift ;;
		-d)
			shift
			MODE="DIR"
			INPUT=$1
			shift ;;
		-o)
			shift
			OUTPUT=$1
			shift ;;
		*)
			break
			;;
	esac
done	

if [[ -z ${MODE} ]]; then
	echo "runtype not given"
	usage
fi

# --------------- #
# Environments 
# --------------- #
# env.
CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-trunk/config"
NLP_CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterms/config"
NLP_DICT="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterms/resource"
DICTIONARY="/Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sentiments"
SYSDICT="/Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sys"

# dependency

DEP=`find ../lib -type f -name "*" | awk '{printf("%s:", $0);}' | sed 's/:$//g'`
TARGET="../target/sentimentAnalyzer-core-1.3.9-SNAPSHOT.jar"
CP="${DEP}:${TARGET}:${CONFIG}:${NLP_CONFIG}:${NLP_DICT}:${DICTIONARY}:${SYSDICT}"

# --------------- #
# Business Logic
# --------------- #

if [[ ${MODE} == "CLI" ]]; then
	java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.NLPUtilTester -c  
elif [[ ${MODE} == "FILE" ]]; then
	if [[ -z ${OUTPUT} ]]; then
		java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.NLPUtilTester -f ${INPUT} 
	else
		java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.NLPUtilTester -f ${INPUT} -o ${OUTPUT} 
	fi
elif [[ ${MODE} == "DIR" ]]; then
	if [[ -z ${OUTPUT} ]]; then
		java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.NLPUtilTester -d ${INPUT} 
	else
		java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} com.skplanet.nlp.sentiment.driver.NLPUtilTester -d ${INPUT} -o ${OUTPUT}
	fi
fi

