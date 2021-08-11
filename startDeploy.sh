#!/bin/bash
echo "Start"
input="./deployConfig.conf"
echo "1"
i=1
while read line
do
echo "2"
if [[ -z $line ]]
    then
        echo "3"
         exit
    fi
  echo "4"
  cp apm-poc-java.jar $i.jar
  echo "5"
  command="nohup java -cp $i.jar yb.Main \"$line\" &"
  echo "6"
  echo $command
  echo "5"
  eval $command
  echo "8"
  let "i++"
  echo "9"
done < $input
echo "10"