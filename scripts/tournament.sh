#!/bin/bash
set -e #exit on error

ME="swarming"
OPPONENTS=("samwho" "ben.one" "rybots" "examplefuncsplayer")
#OPPONENTS=("examplefuncsplayer")
MAPS=("Arena" "Alone")

function extractTime(){
  result=$1
  echo $(echo -e $result | grep -Eo 'Total time: .* secs')
  return 0
}

function resultSummary(){
  result=$1
  timeTaken=$(extractTime "$result")
  lines=$(echo "${result}" | grep -E -A 1 '\[server\].*wins.*')
  summary=$(echo $lines | sed -E 's/\[server\] *//g')
  echo "${summary} (${timeTaken})"
  return 0
}

function buildMatchCommand(){
  team1=$1
  team2=$2
  map=$3
  echo "./gradlew --offline runQuiet -PteamA=${team1} -PteamB=${team2} -Pmaps=${map}"
  return 0
}

function printResult(){
  result=$1
  me=$2
  prepend=$3
  cmnd=$4
  summary=$(resultSummary "$result")
  if [[ $summary == *${me}* ]]; then
    echo -e "\033[0;36m  ${prepend}${summary}\033[0m"
  else
    echo -e "\033[0;31m  ${prepend}${summary}\033[0m"
    echo -e "\033[0;31m     ${cmnd}\033[0m"
  fi
}


echo -e "\033[0;35mStarting Tournament\033[0m"
echo -e "\033[0;35m-------------------\033[0m"
for o in ${OPPONENTS[@]}; do
  for m in ${MAPS[@]}; do
    echo -e "\033[0;37mVs. ${o} on ${m}\033[0m"
    COMMAND_A=$(buildMatchCommand "swarming" $o $m)
    COMMAND_B=$(buildMatchCommand $o "swarming" $m)
    RESULT_A=$($COMMAND_A)
    #RESULT_B=$($COMMAND_B)
    printResult "${RESULT_A}" "$ME" "A: " "$COMMAND_A"
    #printResult "${RESULT_B}" "$ME" "B: " "$COMMAND_B"
    #COMMAND_A="./gradlew --offline runQuiet -PteamA=swarming -PteamB=${o} -Pmaps=${m}"
    #COMMAND_B="./gradlew --offline runQuiet -PteamB=swarming -PteamA=${o} -Pmaps=${m}"
    #WIN_LINES="grep -E -A 1 '\[server\].*wins.*'"
    #TIME_GREP="grep -Eo 'Total time: .*'"
    #SERVER_STRIP=
    #RESULT_A=`${COMMAND_A}`
    #RESULT_B=`${COMMAND_B}`
    #CLEAN_RESULT_A=`echo ${RESULT_A} | ${WIN_LINES} | sed -E 's/\[server\] *//g'`
    #CLEAN_RESULT_B=`echo ${RESULT_B} | eval ${WIN_LINES} | sed -E 's/\[server\] *//g'`
    #TIME_A=`echo ${RESULT_A} | ${TIME_GREP}`
    #TIME_B=`echo ${RESULT_B} | ${TIME_GREP}`
    #if [[ $CLEAN_RESULT_A == *${ME}* ]]; then
    #  echo -e "\033[0;36m  A: ${CLEAN_RESULT_A} (${TIME_A})\033[0m"
    #else
    #  echo -e "\033[0;31m  A: ${CLEAN_RESULT_A} (${TIME_A})\033[0m"
    #  echo -e "\033[0;31m     ${COMMAND_A}\033[0m"
    #fi
    #if [[ $CLEAN_RESULT_B == *${ME}* ]]; then
    #  echo -e "\033[0;36m  B: ${CLEAN_RESULT_B} (${TIME_B})\033[0m"
    #else
    #  echo -e "\033[0;31m  B: ${CLEAN_RESULT_B} (${TIME_B})\033[0m"
    #  echo -e "\033[0;31m     ${COMMAND_B}\033[0m"
    #fi
  done
done
