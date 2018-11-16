#!/usr/bin/env bash

echo "CPU消耗 TOP-N统计 start"
ps aux | head -1;
ps aux | sort -rn -k3 | head -10
echo "CPU消耗 TOP-N统计 end"


echo "Mem消耗 TOP-N统计 start"
ps aux | head -1;
ps aux | sort -rn -k4 | head -10
echo "Mem消耗 TOP-N统计 end"


echo "Swap消耗 TOP-N统计 start"
ps aux | head -1;
ps aux | sort -rn -k5 | head -10
echo "Swap消耗 TOP-N统计 end"

