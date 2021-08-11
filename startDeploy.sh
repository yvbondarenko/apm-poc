#!/bin/bash
kill -9 `ps aux|grep 'apm-poc-java.jar' | awk '$1 {print $2}'`
input="./deployConfig.conf"
i=1
while read line
do

if [[ -z $line ]]
    then
         exit
    fi
  cp apm-poc-java.jar $i.jar
  command="nohup java -cp $i.jar yb.Main \"$line\" &"
  eval r=$command
  let "i++"
done < $input