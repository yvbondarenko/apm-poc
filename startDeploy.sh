#!/bin/bash
JENKINS_NODE_COOKIE=dontKillMe
input="./deployConfig.conf"
i=1
while read line
do
if [[ -z $line ]]
    then
        echo "3"
         exit
    fi
  cp apm-poc-java.jar $i.jar
  command="nohup java -cp $i.jar yb.Main \"$line\" &"
  eval r=$command
  let "i++"
done < $input
