#!/bin/bash
kill -9 `ps aux|grep 'apm-poc-java.jar' | awk '$1 {print $2}'`