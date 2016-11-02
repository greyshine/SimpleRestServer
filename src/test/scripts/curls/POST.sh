#!/bin/bash

curl -X POST -H"Content-Type:application/json" -d'{hallo:"welt"}' http://localhost:7777/hws?_verbose=true
