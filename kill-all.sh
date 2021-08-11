#!/bin/bash
kill -9 `ps aux|grep 'generateIntervalMs' | awk '$1 {print $2}'`