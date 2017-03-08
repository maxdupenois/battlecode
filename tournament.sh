#!/bin/bash
set -e #exit on error

ME="swarming"
OPPONENTS=("samwho" "ben.one")
MAPS=("Arena" "Alone")

echo -e "\033[0;35mStarting Tournament\033[0m"
echo -e "\033[0;35m-------------------\033[0m"
for o in ${OPPONENTS[@]}; do
  for m in ${MAPS[@]}; do
    echo -e "\033[0;37mVs. ${o} on ${m}\033[0m"
    COMMAND="gradle --offline run -PteamA=swarming -PteamB=${o} -Pmaps=${m}"
    RESULT=`${COMMAND} | grep -E -A 1 '\[server\].*wins.*'`
    CLEAN_RESULT=`echo ${RESULT} | sed -E 's/\[server\] *//g'`
    if [[ $CLEAN_RESULT == *${ME}* ]]; then
      echo -e "\033[0;36m  ${CLEAN_RESULT}\033[0m"
    else
      echo -e "\033[0;31m  ${CLEAN_RESULT}\033[0m"
    fi
    echo -e "\033[0;32m  RERUN: ${COMMAND}\033[0m"
  done
done
