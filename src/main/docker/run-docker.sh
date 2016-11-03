#!/bin/sh

# Parameter is homepath of SimpleRestServer
docker run -v $1:/opt/sirese/home -d greyshine/sirese

#docker ps
