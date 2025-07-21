#!/bin/bash

red=$(tput setaf 1)
green=$(tput setaf 2)
reset=$(tput sgr0)

cd ..
cd "services" || exit
echo "current directory $PWD"

services=("analytics" "auth" "billing" "config" "gateway" "patient")

for cur_service in "${services[@]}"; do
  cd "${cur_service}-ms" || exit

  echo "Start building $cur_service-ms image"
  echo "Dockerfile location: $PWD"
  docker build -t "danilchet/$cur_service" . || { echo "${red}Error building $cur_service-ms image${reset}"; return 1; }
  echo "${green} $cur_service image has been successfully built${reset}"

  cd ..
done

