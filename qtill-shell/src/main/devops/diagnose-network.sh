#!/usr/bin/env bash

echo "TCP连接状态统计 start"
netstat -nat | awk '{print $6}' | sort | uniq -c | sort -rn
echo "TCP连接状态统计 end"



diagnost_port=8080
echo "TCP连接状态统计(端口: $diagnost_port) start"
netstat -ant | grep -i "$diagnost_port" | awk '/^tcp/ {++S[$NF]} END {for(a in S) print a, S[a]}'
echo "TCP连接状态统计(端口: $diagnost_port) start"