#!/bin/bash

function usage() {
	echo "Usage: $0 [sheet file] [category id] [output dir.]"
	exit 1
}

if [ $# -ne 3 ]; then
	usage
fi

# create attribute file 
cat $1 | grep -v "^#" | grep "FEA" | sed 's/^FEA://g' | awk 'BEGIN{n=1}{printf("A%d\t%s\n", n++, $0);}' | sed 's/	/,/g' | sed 's/,/	/' > ${3}/attribute-${2}.dict
# create expression file
cat $1 | grep -v "^#" | grep "EXP" | sed 's/EXP://g' | awk 'BEGIN{FS="\t"; n=1}{printf("E%d\t%s\t%s\n", n++, $1, $2);}' > ${3}/expression-${2}.dict
# create mapping file 
cat $1 | grep -v "^#" | grep "EXP\|FEA" | awk 'BEGIN{FS="\t"; attn=1; expn=1}{ if ( $0 ~ /^FEA:/ ) { printf("\nA%d\t", attn); attn++; } else { printf("E%d_%s,", expn, $3); expn++; } }' | sed '/^$/d' | sed 's/,$//g' > ${3}/att-exp-${2}.dict

