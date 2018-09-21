#!/bin/bash
cd /home/data
exec java -jar /deployments/*.jar "$@"
