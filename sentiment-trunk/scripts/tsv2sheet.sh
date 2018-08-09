#!/bin/bash

# run script template

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo "usage: $0 [options]"
	echo "-h	help"
	echo "-c	category number"
	echo "-i	input path"
	echo "-o	output path"
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
		-i)
			shift
			input=$1
			shift ;;
		-o)
			shift
			output=$1
			shift ;;
		*)
			break
			;;
	esac
done	

if [[ -z ${category} ]] || [[ -z ${input} ]] || [[ -z ${output} ]]; then
	usage
fi

# --------------- #
# Environments 
# --------------- #
# env.
CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-analyzer-core/config"
NLP_CONFIG="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterm/trunk/config"
NLP_DICT="/Users/sindongboy/Dropbox/Documents/workspace/nlp_indexterm/trunk/resource"
#DICTIONARY="/Users/sindongboy/Dropbox/Documents/resource/dictionary/sentiments/sentiments"
DICTIONARY="/Users/sindongboy/Dropbox/Documents/workspace/sentiment-resource/dictionary/sentiments"
SYSDICT="/Users/sindongboy/Dropbox/Documents/resource/dictionary/sentiments/sys"

# dependency
LOG4J="/Users/sindongboy/.m2/repository/log4j/log4j/1.2.7/log4j-1.2.7.jar"
OMP_CONFIG="/Users/sindongboy/.m2/repository/com/skplanet/nlp/omp-config/1.0.6-SNAPSHOT/omp-config-1.0.6-SNAPSHOT.jar"
OMP_TRIE="/Users/sindongboy/.m2/repository/com/skplanet/nlp/trie/1.1.0-SNAPSHOT/trie-1.1.0-SNAPSHOT.jar"
CLI="/Users/sindongboy/.m2/repository/com/skplanet/nlp/cli/1.0.0/cli-1.0.0.jar"
COMMONCLI="/Users/sindongboy/.m2/repository/commons-cli/commons-cli/1.2/commons-cli-1.2.jar"
NLP_INDEXTERM="/Users/sindongboy/.m2/repository/com/skplanet/nlp_indexterm/1.4.2/nlp_indexterm-1.4.2.jar"
HNLP="/Users/sindongboy/.m2/repository/com/skplanet/nlp/hnlp/2.0.4-SNAPSHOT/hnlp-2.0.4-SNAPSHOT.jar"
SPELLER="/Users/sindongboy/.m2/repository/com/skplanet/nlp/speller/1.2.0/speller-1.2.0.jar"
BAG="/Users/sindongboy/.m2/repository/net/sourceforge/collections/collections-generic/4.01/collections-generic-4.01.jar"

PACKAGE="com.skplanet.nlp.sentiment"
TARGET="../target/sentimentAnalyzer-core-1.3.6-SNAPSHOT.jar"
CP="$TARGET:$CONFIG:$OMP_CONFIG:$OMP_TRIE:$LOG4J:$NLP_CONFIG:$NLP_DICT:$NLP_INDEXTERM:$HNLP:$SPELLER:${BAG}:${CLI}:${COMMONCLI}:${DICTIONARY}:${SYSDICT}"


# --------------- #
# Business Logic
# --------------- #

# make sure ${JAVA_HOME} set properly
java -Xmx4G -Dfile.encoding=UTF-8 -cp ${CP} ${PACKAGE}.knowledge.TSV2SHEET -c ${category} -t ${input} -o ${output}
