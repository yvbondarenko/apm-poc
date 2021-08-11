#!/bin/bash
kill -9 `ps aux|grep 'generateIntervalMs' |grep -v grep | awk '$1 {print $2}'`