#!/bin/bash
# Boots a test server for each target and checks DamageIndicator works:
#   - the plugin enables and picks the right text and scheduler for the platform
#   - /di reload works
#   - damaging a cow shows "-3" above it, healing it shows "+4"
#   - damaging a pig (blacklisted in the test config) shows nothing
#   - the text disappears again after display-duration
# Then it stops the server and reports problems.
#
#   scripts/smoke-test.sh paper-26.3 spigot-1.19.4
#   scripts/smoke-test.sh all
#
# Targets are <platform>-<minecraft version>; `./gradlew -q printTestServers` lists them.
# Running it accepts the Minecraft EULA (https://aka.ms/MinecraftEULA) for the test servers in run/.
# Needs Java (Gradle downloads the rest) and Python 3.
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

FAILED=0
for T in "${TARGETS[@]}"; do
  PLATFORM=${T%%-*}; VERSION=${T#*-}
  TASK="run$(echo "${PLATFORM:0:1}" | tr a-z A-Z)${PLATFORM:1}_${VERSION//./_}"
  DIR=$ROOT/run/$T
  LOG=$OUT/$T.log; : > "$LOG"
  BUILD_LOG=$OUT/$T.build.log; : > "$BUILD_LOG"
  RCON_LOG=$OUT/$T.rcon.log; : > "$RCON_LOG"
  if [ "$PLATFORM" = spigot ]; then EXPECT_TEXT="colour-coded text"; else EXPECT_TEXT="built-in Adventure"; fi
  if [ "$PLATFORM" = folia ]; then EXPECT_TASKS="Folia's entity scheduler"; else EXPECT_TASKS="the Bukkit scheduler"; fi

  # Fresh world each run, so earlier runs can't affect the result. The plugin config is the default one,
  # except that the text stays for 5 seconds (instead of half a second) so there's time to look at it,
  # and pigs are blacklisted.
  mkdir -p "$DIR/plugins/DamageIndicator"
  rm -rf "$DIR"/world "$DIR"/world_nether "$DIR"/world_the_end "$DIR/plugins/DamageIndicator"/*
  sed 's/^  display-duration: .*/  display-duration: 100/' src/main/resources/config.yml \
    | awk '{ print } /^    - PLAYER$/ { print "    - PIG" }' > "$DIR/plugins/DamageIndicator/config.yml"
  echo "eula=true" > "$DIR/eula.txt"
  printf '%s\n' 'level-type=flat' 'online-mode=false' 'spawn-protection=0' 'server-port=25565' \
    'enable-rcon=true' 'rcon.port=25575' 'rcon.password=smoketest' 'max-tick-time=-1' \
    'generate-structures=false' 'spawn-monsters=false' 'spawn-animals=false' > "$DIR/server.properties"

  # A server left over from an earlier run would take the port
  if lsof -t -iTCP:25565 -sTCP:LISTEN > /dev/null 2>&1 || lsof -t -iTCP:25575 -sTCP:LISTEN > /dev/null 2>&1; then
    echo "FAIL $T (port 25565 or 25575 is already in use; stop the other server first)"; FAILED=1; continue
  fi

  # Build the plugin (and Spigot via BuildTools, the first time) before starting the clock
  if ! ./gradlew shadowJar $( [ "$PLATFORM" = spigot ] && echo "buildSpigot_${VERSION//./_}" ) < /dev/null >> "$BUILD_LOG" 2>&1; then
    echo "FAIL $T (build failed, see $BUILD_LOG)"; FAILED=1; continue
  fi

  ( ./gradlew "$TASK" < /dev/null >> "$LOG" 2>&1; echo "GRADLE_EXIT=$?" >> "$LOG" ) &

  STATUS=timeout
  for _ in $(seq 1 900); do
    if grep -q 'RCON running' "$LOG"; then STATUS=started; break; fi
    if grep -q 'GRADLE_EXIT=' "$LOG"; then STATUS=exited; break; fi
    sleep 1
  done

  PROBLEMS=()
  if [ "$STATUS" = started ]; then
    grep -q 'Enabling DamageIndicator' "$LOG" || PROBLEMS+=("plugin did not enable")
    grep -q "Showing text with .*$EXPECT_TEXT" "$LOG" || PROBLEMS+=("expected text via $EXPECT_TEXT")
    grep -q "removing it with $EXPECT_TASKS" "$LOG" || PROBLEMS+=("expected removal via $EXPECT_TASKS")

    # check <name> <expected output (extended regex)> <commands...>
    check() {
      local name=$1 expect=$2; shift 2
      local out; out=$(rcon "$@"); echo "$out" >> "$RCON_LOG"
      echo "$out" | tr '\n' ' ' | grep -qE "$expect" || PROBLEMS+=("$name")
    }
    check "/di reload failed" 'Reloaded configuration successfully' "di reload"
    rcon "forceload add 0 0" "summon pig 2 -60 2 {NoAI:1b}" "summon cow 0 -60 0 {NoAI:1b,Tags:[\"ditest\"]}" >> "$RCON_LOG"
    check "pig got an indicator (it's blacklisted)" 'Applied 1(\.0)? damage.*Test failed' \
      "damage @e[type=pig,limit=1] 1" "execute if entity @e[type=text_display]"
    check "no damage indicator" '(-3|red.*\[7)' \
      "damage @e[type=cow,tag=ditest,limit=1] 3" "data get entity @e[type=text_display,limit=1] text"
    # Instant Health heals on the cow's next tick, so give it a moment
    rcon "kill @e[type=text_display]" "effect give @e[type=cow,tag=ditest] instant_health 1 0" >> "$RCON_LOG"
    sleep 1
    check "no heal indicator" '(\+4|green.*\[10)' "data get entity @e[type=text_display,limit=1] text"
    sleep 6
    check "indicator did not disappear" 'Test failed' "execute if entity @e[type=text_display]"

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
    echo "PASS $T"
  else
    echo "FAIL $T: $(IFS='; '; echo "${PROBLEMS[*]}") (see $LOG and $RCON_LOG)"
    [ -n "$ERRORS" ] && echo "$ERRORS" | sed 's/^/    /'
    FAILED=1
  fi
done
exit $FAILED
