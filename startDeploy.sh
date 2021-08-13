#!/bin/bash
JENKINS_NODE_COOKIE=dontKillMe
input="./dynatraceDeployConfig.conf"
i=1
while read line
do
if [[ -z $line ]]
    then
         exit
    fi
  eval r=$line
  let "i++"
done < $input
