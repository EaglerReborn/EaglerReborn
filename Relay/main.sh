#!/bin/bash

# smashed together by ayunami2000
# otterdev was here
unset DISPLAY

chmod +x caddy

tmux kill-session -t viaaas
tmux kill-session -t bungee
tmux kill-session -t cors
./caddy stop
tmux kill-session -t caddy

JAVA8="$(command -v javac)"
JAVA8="${JAVA8%?}"

mkdir -p bungee/plugins/EaglercraftXBungee
mkdir -p viaaas

rm -rf viaaas/logs
rm -rf waterfall/logs
ln -s /dev/null viaaas/logs
ln -s /dev/null waterfall/logs
rm bungee/modules/*

if [ ! -f current_version ]; then
  touch current_version
fi

rm latest_version

wget -O latest_version "https://gitlab.com/lax1dude/eaglercraftx-1.8/-/raw/main/gateway_version"

if [ -f latest_version ]; then
  if ! cmp -s "latest_version" "current_version"; then
    rm current_version
    cp latest_version current_version
    rm /tmp/EaglercraftXBungee.jar
    wget -O /tmp/EaglercraftXBungee.jar "https://gitlab.com/lax1dude/eaglercraftx-1.8/-/raw/main/gateway/EaglercraftXBungee/EaglerXBungee-Latest.jar"
    if [ -f /tmp/EaglercraftXBungee.jar ]; then
      rm bungee/plugins/EaglercraftXBungee.jar
      mv /tmp/EaglercraftXBungee.jar bungee/plugins/EaglercraftXBungee.jar
    fi
  fi
  
  rm latest_version
fi

# update waterfall!!
cd bungee
rm bungee-new.jar
WF_VERSION="`curl -s \"https://papermc.io/api/v2/projects/waterfall\" | jq -r \".version_groups[-1]\"`"
WF_BUILDS="`curl -s \"https://papermc.io/api/v2/projects/waterfall/versions/$WF_VERSION/builds\"`"
WF_SHA256="`echo $WF_BUILDS | jq -r \".builds[-1].downloads.application.sha256\"`"
echo "$WF_SHA256 bungee.jar" | sha256sum --check
retVal=$?
if [ $retVal -ne 0 ]; then
  wget -O bungee-new.jar "`echo $WF_BUILDS | jq -r \".builds[-1]|\\\"https://papermc.io/api/v2/projects/waterfall/versions/$WF_VERSION/builds/\\\"+(.build|tostring)+\\\"/downloads/\\\"+.downloads.application.name\"`"
  if [ -f "bungee-new.jar" ]; then
    rm bungee.jar
    mv bungee-new.jar bungee.jar
  fi
fi
cd ..

# update viaaas!!
cd viaaas
rm /tmp/viaaas.jar
wget -O /tmp/viaaas.jar "https://jitpack.io/com/github/ViaVersion/VIAaaS/master-SNAPSHOT/VIAaaS-master-SNAPSHOT-all.jar"
if [ -f /tmp/viaaas.jar ]; then
  rm viaaas.jar
  mv /tmp/viaaas.jar viaaas.jar
fi
cd ..

# run it!!
tmux new -d -s caddy "./caddy run --config ./Caddyfile"
cd cors
tmux new -d -s cors "node app.js"
cd ../bungee
tmux new -d -s bungee "java -Xmx128M -jar bungee.jar"
cd ../viaaas
java -Xmx512M -jar viaaas.jar -host=127.0.0.1 -port=8082
cd ..
tmux kill-session -t bungee
tmux kill-session -t cors
./caddy stop
tmux kill-session -t caddy
