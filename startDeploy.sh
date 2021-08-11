#!/bin/bash
kill -9 `ps aux|grep 'apm-poc-java.jar' | awk '$1 {print $2}'`
input="./deployConfig.conf"
while read line
do
if [[ -z $line ]]
    then
         exit
    fi
  command="nohup java -cp apm-poc-java.jar yb.Main \"$line\" &"
  eval r=$command
done < $input