#!/bin/bash
set -e #exit on error

WEBHOOK_URL="https://hooks.slack.com/services/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"
SLACK_CHANNEL="#battlecode-results"


BOTS=("swarming" "samwho" "ben.one" "rybots")
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

for b1 in ${BOTS[@]}; do
  RESULTS="${RESULTS}\n\r*Testing ${b1} _(Team A)_*\n\r*-------------*"
  for b2 in ${BOTS[@]}; do
    if [[ $b1 == $b2 ]]; then continue; fi
    for m in ${MAPS[@]}; do
      echo "${b1} vs ${b2} on ${m}"
      COMMAND="./gradlew run -PteamA=${b1} -PteamB=${b2} -Pmaps=${m}"
      RESULT=`${COMMAND} | grep -E -A 1 '\[server\].*wins.*'`
      CLEAN_RESULT=`echo ${RESULT} | sed -E 's/\[server\] *//g'`
      if [[ $CLEAN_RESULT == *${b1}* ]]; then
        RESULTS="${RESULTS}\n\r    :smile: *WIN * against ${b2} on ${m} :: ${CLEAN_RESULT}"
      else
        RESULTS="${RESULTS}\n\r    :rage: *LOSS* against ${b2} on ${m} :: ${CLEAN_RESULT}"
      fi
    done
  done
done
echo -e $RESULTS

payload="payload={\"channel\":\"${SLACK_CHANNEL}\",
\"username\":\"Battlecode Tournament Result\",
\"icon_emoji\":\":robot_face:\",
\"text\":\"${RESULTS}\"
}"

curl -X POST --data-urlencode "${payload}" ${WEBHOOK_URL}
