#!/bin/bash
set -e #exit on error

WEBHOOK_URL="https://hooks.slack.com/services/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"
SLACK_CHANNEL="#battlecode-results"


BOTS=("swarming" "samwho" "ben.one" "rybots")
#BOTS=("swarming" "examplefuncsplayer")
MAPS=("Arena" "Alone")
RESULTS=""

mkdir -p ./others
cd others
if [[ -d ben ]]; then
  cd ben && git pull --rebase && cd ..
else
  git clone https://github.com/benashford/battlecode-2017.git ben
fi

if [[ -d samwho ]]; then
  cd samwho && git pull --rebase && cd ..
else
  git clone https://github.com/samwho/battlecode-2017.git samwho
fi

if [[ -d rybots ]]; then
  cd rybots && git pull --rebase && cd ..
else
  git clone https://github.com/Rylon/rybots.git
fi

cd ../src/
if [[ -e ben ]]; then rm ben; fi
if [[ -e rybots ]]; then rm rybots; fi
if [[ -e samwho ]]; then rm samwho; fi

ln -s ../others/ben/src/ben ./ben
ln -s ../others/samwho/src/samwho ./samwho
ln -s ../others/rybots/src/rybots ./rybots
cd ..
echo "Running tournament"

function buildMatchCommand(){
  team1=$1
  team2=$2
  map=$3
  echo "./gradlew runQuiet -PteamA=${team1} -PteamB=${team2} -Pmaps=${map}"
  return 0
}

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
spin='-\|/'

for b1 in ${BOTS[@]}; do
  RESULTS="${RESULTS}\n\r*Testing ${b1} _(Team A)_*\n\r*-------------*"
  for b2 in ${BOTS[@]}; do
    if [[ $b1 == $b2 ]]; then continue; fi
    for m in ${MAPS[@]}; do
      echo "${b1} vs ${b2} on ${m}"
      COMMAND=$(buildMatchCommand "$b1" "$b2" "$m")
      tmp=$(mktemp)
      resultTmp=$(mktemp)
      ($COMMAND >"$resultTmp"; echo $? >"$tmp") &
      pid=$!
      i=0
      while kill -0 $pid 2>/dev/null
      do
        i=$(( (i+1) %4 ))
        printf "\r[playing] ${spin:$i:1}"
        sleep .1
      done
      echo -e "\rGame Complete"
      read ret <"$tmp"
      RESULT="$(cat $resultTmp)"
      if [[ $ret != 0 ]]; then return $ret; fi

      SUMMARY=$(resultSummary "$RESULT")
      if [[ $SUMMARY == *${b1}* ]]; then
        RESULTS="${RESULTS}\n\r    :smile: *WIN * against ${b2} on ${m} :: ${SUMMARY}"
      else
        RESULTS="${RESULTS}\n\r    :rage: *LOSS* against ${b2} on ${m} :: ${SUMMARY}"
      fi

      #COMMAND="./gradlew runQuiet -PteamA=${b1} -PteamB=${b2} -Pmaps=${m}"
      #RESULT=`${COMMAND} | grep -E -A 1 '\[server\].*wins.*'`
      #CLEAN_RESULT=`echo ${RESULT} | sed -E 's/\[server\] *//g'`
      #if [[ $CLEAN_RESULT == *${b1}* ]]; then
      #  RESULTS="${RESULTS}\n\r    :smile: *WIN * against ${b2} on ${m} :: ${CLEAN_RESULT}"
      #else
      #  RESULTS="${RESULTS}\n\r    :rage: *LOSS* against ${b2} on ${m} :: ${CLEAN_RESULT}"
      #fi
    done
  done
done
echo -e $RESULTS

#payload="payload={\"channel\":\"${SLACK_CHANNEL}\",
#\"username\":\"Battlecode Tournament Result\",
#\"icon_emoji\":\":robot_face:\",
#\"text\":\"${RESULTS}\"
#}"
#
#curl -X POST --data-urlencode "${payload}" ${WEBHOOK_URL}
