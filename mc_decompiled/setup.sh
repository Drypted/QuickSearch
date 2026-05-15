#!/bin/zsh

# fetch cfr if not present
if [ ! -f "cfr.jar" ]; then
    echo "CFR not found, downloading..."
    curl https://www.benf.org/other/cfr/cfr-0.152.jar -o cfr.jar
else
    echo "CFR already present, skipping download."
fi

# check if client.jar exists
if [ ! -f "client.jar" ]; then
    echo "client.jar not found! Please place the client.jar file in this directory and run this script again."
    exit 1
fi

# decompile
echo "Decompiling client.jar..."
java -jar cfr.jar client.jar --outputdir src