#!/bin/bash

BASE="`pwd`/../"

echo "${BASE}"


rm -vf ${BASE}/lib/*

cd ${BASE}
mvn clean install
cp -vf ${BASE}/target/sentimentAnalyzer-core-1.5.0-SNAPSHOT.jar ${BASE}/lib/

cd ${BASE}/lib/
tar zcvf lib.tar.gz *
if [[ -f ${BASE}/target/lib.tar.gz ]]; then
	rm -f ${BASE}/target/lib.tar.gz
fi
mv lib.tar.gz ../target/

