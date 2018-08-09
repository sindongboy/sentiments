#!/bin/bash

# ------------------------------------ #
# compiled dict ==> nlp-compiled dict.
# ------------------------------------ #

function usage() {
	echo "usage: $0 [options]"
	echo "-h	help"
	echo "-c	category id"
	echo "-i	input base dir"
	echo "-o	output base dir"
	exit 1
}

while test $# -gt 0;
do
	case "$1" in
		-h)
			usage
			;;
		-i)
			shift
			input=$1
			shift ;;
		-o)
			shift
			output=$1
			shift ;;
		-c)
			shift 
			category=$1
			shift ;;
		*)
			break
			;;
	esac
done

if [[ -z ${input} ]] || [[ -z ${output} ]] || [[ -z ${category} ]]; then
	usage
fi

java -Dfile.encoding=UTF-8 -Xmx4G -cp ../target/sentiment-dict-compiler-1.0.0-jar-with-dependencies.jar:../../nlp_indexterm/trunk/config:../../nlp_indexterm/trunk/resource:../config:../../sentiment-resources/resource/sys com.skplanet.nlp.DictPreprocessor -c ${category} -i ${input} -o ${output}
