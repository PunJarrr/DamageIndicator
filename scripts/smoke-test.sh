#!/bin/bash
# Boots a test server for each target and checks DamageIndicator works:
#   - the plugin enables and picks the right text and scheduler for the platform
#   - /di reload works
#   - damaging a cow shows "-3" above it, healing it shows "+4"
#   - damaging a pig (blacklisted in the test config) shows nothing
#   - the text disappears again after display-duration
#   - an old (1.0.0) config.yml is updated, with its settings kept and a backup saved
# These run over RCON (the server console), and again as a test bot player that checks the text it receives.
# Folia's console can't reach entities, so on Folia only the bot runs them.
# Then it stops the server and reports problems.
#
#   scripts/smoke-test.sh paper-26.3 spigot-1.19.4
#   scripts/smoke-test.sh all
#
# Targets are <platform>-<minecraft version>; `./gradlew -q printTestServers` lists them.
# Running it accepts the Minecraft EULA (https://aka.ms/MinecraftEULA) for the test servers in run/.
# Needs Java (Gradle downloads the rest), Python 3 and Node.js 18+.
set -u
ROOT=$(cd "$(dirname "$0")/.." && pwd)
OUT=$ROOT/build/smoke-test
mkdir -p "$OUT"
cd "$ROOT" || exit 2

TARGETS=("$@")
if [ "${1:-}" = all ]; then
  TARGETS=($(./gradlew -q printTestServers))
fi
[ ${#TARGETS[@]} -eq 0 ] && { echo "usage: $0 <platform-version>... | all"; exit 2; }

rcon() { python3 -I scripts/rcon.py 25575 smoketest "$@" 2>&1; }

# The bot's dependencies (Mineflayer), pinned by tools/smoke-bot/package-lock.json
if [ ! -d tools/smoke-bot/node_modules ]; then
  (cd tools/smoke-bot && npm ci --no-audit --no-fund > "$OUT/npm.log" 2>&1) || { echo "npm ci failed, see $OUT/npm.log"; exit 2; }
fi

# Offline-mode UUID of SmokeBot, so it can be made an operator before it joins
BOT_UUID=$(python3 -I -c 'import hashlib, uuid; print(uuid.UUID(bytes=hashlib.md5(b"OfflinePlayer:SmokeBot").digest(), version=3))')

FAILED=0
for T in "${TARGETS[@]}"; do
  PLATFORM=${T%%-*}; VERSION=${T#*-}
  TASK="run$(echo "${PLATFORM:0:1}" | tr a-z A-Z)${PLATFORM:1}_${VERSION//./_}"
  # Its own folder, so the servers you play on (run/<server>, ./gradlew runServer) are never changed
  DIR=$ROOT/run/smoke/$T
  LOG=$OUT/$T.log; : > "$LOG"
  BUILD_LOG=$OUT/$T.build.log; : > "$BUILD_LOG"
  RCON_LOG=$OUT/$T.rcon.log; : > "$RCON_LOG"
  if [ "$PLATFORM" = spigot ]; then EXPECT_TEXT="colour-coded text"; else EXPECT_TEXT="built-in Adventure"; fi
  if [ "$PLATFORM" = folia ]; then EXPECT_TASKS="Folia's entity scheduler"; else EXPECT_TASKS="the Bukkit scheduler"; fi

  # Fresh world each run, so earlier runs can't affect the result. The plugin gets the config file from
  # DamageIndicator 1.0.0, so every run also checks it's updated. Two settings differ from the defaults and
  # must survive the update: the text stays for 5 seconds (instead of half a second) so there's time to look
  # at it, and pigs are blacklisted.
  mkdir -p "$DIR/plugins/DamageIndicator"
  rm -rf "$DIR"/world "$DIR"/world_nether "$DIR"/world_the_end "$DIR/plugins/DamageIndicator"/*
  sed 's/^  display-duration: .*/  display-duration: 100/' src/test/resources/config-1.0.0.yml \
    | awk '{ print } /^    - PLAYER$/ { print "    - PIG" }' > "$DIR/plugins/DamageIndicator/config.yml"
  echo "eula=true" > "$DIR/eula.txt"
  # server-ip keeps the game port and RCON (password "smoketest") reachable from this computer only
  printf '%s\n' 'server-ip=127.0.0.1' 'level-type=flat' 'online-mode=false' 'enforce-secure-profile=false' 'spawn-protection=0' 'server-port=25565' \
    'enable-rcon=true' 'rcon.port=25575' 'rcon.password=smoketest' 'max-tick-time=-1' \
    'generate-structures=false' 'spawn-monsters=false' > "$DIR/server.properties"
  printf '[{"uuid":"%s","name":"SmokeBot","level":4,"bypassesPlayerLimit":false}]\n' "$BOT_UUID" > "$DIR/ops.json"

  # A server left over from an earlier run would take the port
  if lsof -t -iTCP:25565 -sTCP:LISTEN > /dev/null 2>&1 || lsof -t -iTCP:25575 -sTCP:LISTEN > /dev/null 2>&1; then
    echo "FAIL $T (port 25565 or 25575 is already in use; stop the other server first)"; FAILED=1; continue
  fi

  # Build the plugin (and Spigot via BuildTools, the first time) before starting the clock
  if ! ./gradlew shadowJar $( [ "$PLATFORM" = spigot ] && echo "buildSpigot_${VERSION//./_}" ) < /dev/null >> "$BUILD_LOG" 2>&1; then
    echo "FAIL $T (build failed, see $BUILD_LOG)"; FAILED=1; continue
  fi

  ( ./gradlew -PtestServerRoot=run/smoke "$TASK" < /dev/null >> "$LOG" 2>&1; echo "GRADLE_EXIT=$?" >> "$LOG" ) &

  STATUS=timeout
  for _ in $(seq 1 900); do
    if grep -q 'RCON running' "$LOG"; then STATUS=started; break; fi
    if grep -q 'GRADLE_EXIT=' "$LOG"; then STATUS=exited; break; fi
    sleep 1
  done

  PROBLEMS=(); NOTE=""
  if [ "$STATUS" = started ]; then
    grep -q 'Enabling DamageIndicator' "$LOG" || PROBLEMS+=("plugin did not enable")
    grep -q "Showing text with .*$EXPECT_TEXT" "$LOG" || PROBLEMS+=("expected text via $EXPECT_TEXT")
    grep -q "removing it with $EXPECT_TASKS" "$LOG" || PROBLEMS+=("expected removal via $EXPECT_TASKS")
    CONFIG=$DIR/plugins/DamageIndicator/config.yml
    grep -q 'Updated config.yml from 1.0.0 to 1.1.0' "$LOG" || PROBLEMS+=("old config.yml was not updated")
    grep -q '^config-version: 1.1.0$' "$CONFIG" && grep -q '^  display-duration: 100$' "$CONFIG" \
      && grep -q '^    - PIG$' "$CONFIG" && grep -q '^    damage: "<red>-{damage}❤ <aqua>\[{current}❤\]"$' "$CONFIG" \
      || PROBLEMS+=("updated config.yml lost a setting or kept the old default text")
    ls "$DIR"/plugins/DamageIndicator/backup/config-1.0.0-*.yml > /dev/null 2>&1 || PROBLEMS+=("no backup of the old config")

    # check <name> <expected output (extended regex)> <commands...>: runs the commands once
    check() {
      local name=$1 expect=$2; shift 2
      local out; out=$(rcon "$@"); echo "$out" >> "$RCON_LOG"
      echo "$out" | tr '\n' ' ' | grep -qE "$expect" || PROBLEMS+=("$name")
    }
    # poll <name> <expected output> <seconds> <command>: retries until it matches. A freshly started server
    # can run slower than 20 ticks a second, so anything that waits for game ticks is polled.
    poll() {
      local name=$1 expect=$2 seconds=$3 cmd=$4 out
      for _ in $(seq 1 "$seconds"); do
        out=$(rcon "$cmd")
        echo "$out" | tr '\n' ' ' | grep -qE "$expect" && { echo "$out" >> "$RCON_LOG"; return; }
        sleep 1
      done
      echo "$out" >> "$RCON_LOG"; PROBLEMS+=("$name")
    }
    if [ "$PLATFORM" != folia ]; then
    check "/di reload failed" 'Reloaded configuration successfully' "di reload"
    # Newer vanilla (and Spigot) loads a force-loaded chunk a little later; summon only once it's there
    rcon "forceload add 0 0" >> "$RCON_LOG"
    poll "chunk 0 0 did not load" 'Test passed' 20 "execute if loaded 0 -60 0"
    rcon "summon pig 2 -60 2 {NoAI:1b,Tags:[\"ditest\"]}" "summon cow 0 -60 0 {NoAI:1b,Tags:[\"ditest\"]}" >> "$RCON_LOG"
    # Damage is shown straight away, during the /damage command
    check "pig got an indicator (it's blacklisted)" 'Applied 1(\.0)? damage.*Test failed' \
      "damage @e[type=pig,tag=ditest,limit=1] 1" "execute if entity @e[type=text_display]"
    check "no damage indicator" '(-3|red.*\[7)' \
      "damage @e[type=cow,tag=ditest,limit=1] 3" "data get entity @e[type=text_display,limit=1] text"
    # Instant Health heals on the cow's next tick
    rcon "kill @e[type=text_display]" "effect give @e[type=cow,tag=ditest] instant_health 1 0" >> "$RCON_LOG"
    poll "no heal indicator" '(\+4|green.*\[10)' 10 "data get entity @e[type=text_display,limit=1] text"
    # display-duration is 100 ticks (5 seconds at full speed)
    poll "indicator did not disappear" 'Test failed' 20 "execute if entity @e[type=text_display]"
    fi

    BOT=$(node tools/smoke-bot/bot.js 25565 2>&1); BOT_EXIT=$?
    echo "$BOT" > "$OUT/$T.bot.log"
    case $BOT_EXIT in
      0) NOTE="bot ok" ;;
      3) if [ "$PLATFORM" = folia ]; then NOTE="bot skipped: Mineflayer can't join $VERSION, so only startup was checked"
         else NOTE="bot skipped: Mineflayer can't join $VERSION"; fi ;;
      *) NOTE="bot failed"; PROBLEMS+=("$(echo "$BOT" | grep -E '^(FAIL|SKIP)' | head -1)") ;;
    esac

    rcon stop > /dev/null
  else
    PROBLEMS+=("server $STATUS")
  fi
  for _ in $(seq 1 60); do grep -q 'GRADLE_EXIT=' "$LOG" && break; sleep 1; done
  # If "stop" over RCON didn't work, stop the server process directly (it still saves and shuts down)
  if ! grep -q 'GRADLE_EXIT=' "$LOG"; then
    SERVER_PID=$(lsof -t -iTCP:25565 -sTCP:LISTEN 2>/dev/null | head -1)
    [ -n "$SERVER_PID" ] && kill "$SERVER_PID"
    for _ in $(seq 1 120); do grep -q 'GRADLE_EXIT=' "$LOG" && break; sleep 1; done
    if [ -n "$SERVER_PID" ] && kill -0 "$SERVER_PID" 2>/dev/null; then kill -9 "$SERVER_PID"; PROBLEMS+=("server did not stop"); fi
  fi

  # Any error or exception in the server log fails the test
  IGNORE=$(grep -vE '^(#|$)' scripts/smoke-test-ignore.txt | paste -sd'|' -)
  ERRORS=$(sed 's/\x1b\[[0-9;]*m//g' "$LOG" | grep -E '(ERROR\]|/ERROR|SEVERE\]|Exception)' | grep -vE "${IGNORE:-^\$}" | head -5)
  [ -n "$ERRORS" ] && PROBLEMS+=("errors in the server log")

  if [ ${#PROBLEMS[@]} -eq 0 ]; then
    echo "PASS $T ($NOTE)"
  else
    echo "FAIL $T: $(IFS='; '; echo "${PROBLEMS[*]}") (see $LOG and $RCON_LOG)"
    [ -n "$ERRORS" ] && echo "$ERRORS" | sed 's/^/    /'
    FAILED=1
  fi
done
exit $FAILED
