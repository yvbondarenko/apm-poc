#!/bin/bash
count=$(ps aux|grep 'generateIntervalMs' |grep -v grep | awk '$1 {print $2}' | wc -l)
if (( $count > 0 )); then
    kill -9 `ps aux|grep 'generateIntervalMs' |grep -v grep | awk '$1 {print $2}'`
else
    echo "No process"
fi