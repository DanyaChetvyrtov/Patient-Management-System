#!/bin/bash

#!/usr/bin/env bash

if [ $# -eq 0 ]; then
    echo "Error: you should add arguments!"
    echo "Example: ./docker-build-specific.sh <service-name>"
    exit 1
fi

cd ..
cd "services/$1-ms" || { echo "Error entering $1-ms"; return 1; }
echo "Start building $1-ms image"
echo "Dockerfile location: $PWD"
docker build -t "danilchet/$1-ms" . || { echo "Error building $1-ms image"; return 1; }
echo "$1-ms image has been successfully built"
return 0;