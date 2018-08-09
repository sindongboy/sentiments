#!/bin/bash

# run script template

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo ""
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
		*)
			break
			;;
	esac
done	

# --------------- #
# Environments 
# --------------- #
# env.
REPO="/path/to/the/.m2/repository"
NLP_REPO="$REPO/com/skplanet/nlp"
CONFIG="/path/to/the/config"
NLP_CONFIG="/path/to/the/nlp/config"
NLP_DICT="/path/to/the/nlp/resource"
DICTIONARY="/path/to/the/dictionary"
SYSDICT="/path/to/the/sys/dictionary"
# dependency
LOG4J="$REPO/log4j/log4j/1.2.7/log4j-1.2.7.jar"
OMP_CONFIG="${NLP_REPO}/omp-config/1.0.6-SNAPSHOT/omp-config-1.0.6-SNAPSHOT.jar"
OMP_TRIE="${NLP_REPO}/trie/1.0.1-SNAPSHOT/trie-1.0.1-SNAPSHOT.jar"
CLI="${NLP_REPO}/cli/1.0.0/cli-1.0.0.jar"
COMMONCLI="${REPO}/commons-cli/commons-cli/1.2/commons-cli-1.2.jar"
NLP_INDEXTERM="${REPO}/com/skplanet/nlp_indexterm/1.3.8/nlp_indexterm-1.3.8.jar"
HNLP="${NLP_REPO}/hnlp/2.0.3-SNAPSHOT/hnlp-2.0.3-SNAPSHOT.jar"
SPELLER="${NLP_REPO}/speller/1.2.0/speller-1.2.0.jar"
BAG="${REPO}/net/sourceforge/collections/collections-generic/4.01/collections-generic-4.01.jar"
PACKAGE="com.skplanet.nlp.sentiment"
VERSION=`ls -1 ../target | grep "jar$" | grep -o "[0-9][^j]*" | sed 's/\.$//g'`
TARGET="../target/sentimentAnalyzer-core-${VERSION}.jar"
CP="$TARGET:$CONFIG:$OMP_CONFIG:$OMP_TRIE:$LOG4J:$NLP_CONFIG:$NLP_DICT:$NLP_INDEXTERM:$HNLP:$SPELLER:${BAG}:${CLI}:${COMMONCLI}:${DICTIONARY}:${SYSDICT}"


# --------------- #
# Business Logic
# --------------- #
