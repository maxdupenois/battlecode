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
    RESULT_B=$($COMMAND_B)
    printResult "${RESULT_A}" "$ME" "A: " "$COMMAND_A"
    printResult "${RESULT_B}" "$ME" "B: " "$COMMAND_B"
  done
done
