#!/bin/bash
JENKINS_NODE_COOKIE=dontKillMe
input="./deployConfig.conf"
i=1
while read line
do
if [[ -z $line ]]
    then
         exit
    fi
  cp apm-poc-java.jar $i.jar
  dParam=$(echo $line | cut -f1 -d";")
  command="nohup java -D$dParam -cp $i.jar yb.Main \"$line\" &"
  eval r=$command
  let "i++"
done < $input
