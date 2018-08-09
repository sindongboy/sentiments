#!/bin/bash

# run script template

# --------------- #
# Functions 
# --------------- #
function usage() {
	echo "usage: $0"
	exit 1
}

# --------------- #
# Interface 
# --------------- #

for category in `ls -1 /Users/sindongboy/Dropbox/Documents/resource/sentiment-resource/dictionary/sentiments/ | grep -o "[0-9][0-9]*" | sort -nu | awk '{printf("%s ", $0);}'`
do 
	echo -n "compile: ${category} "
	./preprocess.sh -c ${category}
	echo "done"
done
