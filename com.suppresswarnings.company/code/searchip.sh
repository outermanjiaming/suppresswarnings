#!/bin/sh
var=$(cat /boot/host)
IP=$(ifconfig | grep 'inet' | grep -v '127.0.0.1'| grep -v 'inet6'|awk 'gsub(/^ *| *$/,"")')
url="http://suppresswarnings.com/pi.http?action=raspberrypi&ip="${IP}"&var="${var}
for i in $(seq 1 1000)
do
  echo "ping ping ping"$i >> /boot/print.log 
  pong=$(ping -c 1 "suppresswarnings.com" | grep "transmitted" | awk '{print $4}')
  if (( pong=1 ));then
    curl $url
    echo "curl done"$i >> /boot/print.log
    break 
  else
    echo "fail to ping..."$i >> /boot/print.log
  fi
done
