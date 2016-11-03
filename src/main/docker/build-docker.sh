#!/bin/sh

docker rmi -f greyshine/sirese
docker build -t greyshine/sirese .
docker images