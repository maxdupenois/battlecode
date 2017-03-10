#!/bin/bash
set -e #exit on error

ME="swarming"
OPPONENTS=("samwho" "ben.one" "rybots" "examplefuncsplayer")
MAPS=("Arena" "Alone")

echo -e "\033[0;35mStarting Tournament\033[0m"
echo -e "\033[0;35m-------------------\033[0m"
for o in ${OPPONENTS[@]}; do
  for m in ${MAPS[@]}; do
    echo -e "\033[0;37mVs. ${o} on ${m}\033[0m"
    COMMAND_A="./gradlew --offline runQuiet -PteamA=swarming -PteamB=${o} -Pmaps=${m}"
    COMMAND_B="./gradlew --offline runQuiet -PteamB=swarming -PteamA=${o} -Pmaps=${m}"
    RESULT_A=`${COMMAND_A} | grep -E -A 1 '\[server\].*wins.*'`
    RESULT_B=`${COMMAND_B} | grep -E -A 1 '\[server\].*wins.*'`
    CLEAN_RESULT_A=`echo ${RESULT_A} | sed -E 's/\[server\] *//g'`
    CLEAN_RESULT_B=`echo ${RESULT_B} | sed -E 's/\[server\] *//g'`
    if [[ $CLEAN_RESULT_A == *${ME}* ]]; then
      echo -e "\033[0;36m  A: ${CLEAN_RESULT_A}\033[0m"
    else
      echo -e "\033[0;31m  A: ${CLEAN_RESULT_A}\033[0m"
      echo -e "\033[0;31m     ${COMMAND_A}\033[0m"
    fi
    if [[ $CLEAN_RESULT_B == *${ME}* ]]; then
      echo -e "\033[0;36m  B: ${CLEAN_RESULT_B}\033[0m"
    else
      echo -e "\033[0;31m  B: ${CLEAN_RESULT_B}\033[0m"
      echo -e "\033[0;31m     ${COMMAND_B}\033[0m"
    fi
  done
done
