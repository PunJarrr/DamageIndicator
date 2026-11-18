# Contributing to DamageIndicator

Thanks for helping out! Bug reports and ideas go in the
[issue tracker](https://github.com/PunJarrr/DamageIndicator/issues). This page explains how to build the
plugin, test it and release it.

## How the project works

- One jar supports every server and version. It is compiled against the **oldest and smallest** API we
  support, Spigot 1.19.4 (the first version with text display entities), so anything that compiles also
  exists on Paper, Purpur, Folia and every newer version. The jar targets Java 17, which Minecraft 1.19.4
  servers run on.
- MiniMessage (part of [Adventure](https://docs.papermc.io/adventure/)) is bundled into the jar and
  renamed to `dev.punjarrr.damageIndicator.libs.kyori`, so it never clashes with the copy Paper ships.
- Platform differences live in `src/main/java/.../platform/`:
  - `TextBridge` hands text to the server: `NativeTextBridge` uses the server's own Adventure text on
    Paper, Purpur and Folia; `LegacyTextBridge` uses colour codes on plain Spigot.
  - `Tasks` removes the text later: the Bukkit scheduler, or each entity's own scheduler on Folia.
  The server log says which are used: `Showing text with ..., removing it with ...`.
- `gradle.properties` holds the plugin version, the Minecraft versions shown on the store pages and the
  store page IDs.
- `store/` holds the store page descriptions (Modrinth, Hangar, SpigotMC).

Java 17 is needed to build. The test servers also need Java 21 (1.20.5 – 1.21.x) and Java 25 (26.x);
Gradle downloads any that are missing.

## Common tasks

Build the plugin into `build/libs/DamageIndicator-<version>.jar` and run the unit tests:

```bash
./gradlew build
```

Run a test server with the plugin (the newest Paper, or a specific one):

```bash
./gradlew runServer
```

```bash
./gradlew runPaper_1_19_4
```

List every test server:

```bash
./gradlew -q printTestServers
```

Test servers are Paper 1.19.4 – 26.3, plus Purpur, Folia and Spigot at the oldest and newest versions
(and Folia 26.1.2, the newest the test bot can join).
Each runs in `run/<platform>-<version>/`. Spigot is built with Spigot's BuildTools the first time, which
takes a few minutes.

## Smoke test

`scripts/smoke-test.sh` boots each test server and checks that:

1. the plugin enables, using the right text path (Paper's Adventure, or colour codes on Spigot) and the
   right scheduler (Folia's entity scheduler on Folia)
2. `/di reload` answers "Reloaded configuration successfully!"
3. hitting a blacklisted pig with `/damage` shows no text
4. hitting a cow for 3 damage shows `-3❤ [7❤]`, and healing it with Instant Health shows `+4❤ [10❤]`
5. the text is gone again after `display-duration`
6. the server log has no errors (harmless known lines are listed in `scripts/smoke-test-ignore.txt`)
7. an old config file is updated: each server starts with the `config.yml` from DamageIndicator 1.0.0
   (`src/test/resources/config-1.0.0.yml`), which must come out as `config-version: 1.2.0` with its settings
   kept, the new default text, the new `show-numbers-by-default` option, and a backup in `backup/`.
8. (test bot only) after `/di`, the bot gets no text when the cow is hit, also after leaving and joining
   again, and after `/di` once more it gets `-1❤ [7❤]`.
9. (test bot only) with `PLAYER` taken off the blacklist, the bot hurts itself: a number is made
   (`execute if entity` finds it) but the bot isn't sent it (`hide-own-indicators`).
10. PlaceholderAPI: every test server gets it, except paper-1.20.6, folia-1.21.11 and spigot-1.21.11, which
   check the plugin works without it. With it, the plugin must hook in, and the bot checks
   `%damageindicator_status%` in the damage text (`-3❤ [7❤] ON`), `%damageindicator_shown%` in the
   `/di reload` message, and `/papi parse me %damageindicator_status%` before and after `/di`.

Checks 2 – 5 run twice: over RCON (the server console), and as a test bot
([Mineflayer](https://github.com/PrismarineJS/mineflayer), in `tools/smoke-bot/`) that joins as a player
and reads the floating text it actually receives.

- Folia's console can't reach entities (and on 1.19.4 can't run commands at all), so on Folia only the
  bot runs checks 2 – 5.
- Mineflayer can't join Minecraft versions newer than it supports (currently 26.2 and 26.3). Those
  servers say "bot skipped". On Paper and Spigot the RCON checks still run there; on **Folia 26.3 only
  checks 1 and 6 run**, and the report says so.

For the test, that old config has `display-duration` set to 100 ticks so there's time to look, and `PIG`
added to the blacklist. Both must survive the update.

The smoke test uses its own server folders, `run/smoke/<server>/`, so it never changes the servers you play
on with `./gradlew runServer` (`run/<server>/`). Its servers only accept connections from your own computer.

Running it accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for the test servers. It needs
Python 3 and Node.js 18 or newer.

```bash
scripts/smoke-test.sh paper-26.3 spigot-1.19.4
```

```bash
scripts/smoke-test.sh all
```

Logs are written to `build/smoke-test/`. The GitHub `Build` workflow runs the smoke test on every server
for each push.

## In-game checklist

The smoke test can't see the text or check every setting. Before a release, join a test server
(`./gradlew runServer`, then connect to `localhost`) and check:

- [ ] Hitting a mob shows a red `-<damage>❤` and the health left in aqua, just above its head, for half a
      second.
- [ ] Feeding a damaged wolf or horse, or splashing a mob with Healing, shows a green `+<heal>❤`.
- [ ] The text faces you as you walk around it (`billboard: CENTER`). Set `billboard: VERTICAL`,
      `/di reload`: it only turns sideways. `HORIZONTAL` only tilts up and down; `FIXED` never turns.
- [ ] Set `billboard: CENTRE` (a typo), `/di reload`: the server log warns about it and the text still
      shows.
- [ ] Hitting another player or an armor stand shows nothing.
- [ ] Set `number-format: double`, `/di reload`: numbers show decimals.
- [ ] Set `scale: 2.0`, `/di reload`: the text is twice as big.
- [ ] A gradient such as `<gradient:red:gold>-{damage}</gradient>` shows as a gradient, on Paper and on
      Spigot (`./gradlew runSpigot_1_21_11`).
- [ ] With [WorldGuard](https://enginehub.org/worldguard), run `/rg flag __global__ damage-animals deny`,
      then hit a cow as a non-operator: the cow takes no damage and no text shows.
- [ ] Hit a mob, then stop the server within half a second (`stop`). After starting again, no text is
      left floating.
- [ ] As a non-operator, `/di reload` says you don't have permission.
- [ ] Type `/di` (as anyone): it says numbers are now off, and hitting a mob shows you nothing. A second
      player nearby still sees the numbers. Rejoin: still off. `/di` again: back on.
- [ ] Set `show-numbers-by-default: false`, `/di reload`: a player who never used `/di` sees no numbers, and
      `/di` turns them on for them. Players who already chose keep their choice.
- [ ] `/di` in the server console says only players can turn numbers on or off.
- [ ] With `damageindicator.toggle` taken away (e.g. with LuckPerms), `/di` says you don't have permission.
- [ ] With PlaceholderAPI (the test servers have it): `/papi ecloud download Player`, `/papi reload`, then
      set the damage text to `<red>-{damage}❤ <gray>%player_name%`, `/di reload`. Hitting a mob shows your
      name; a mob hurt by something else (fall, `/damage` from the console) shows the text without a name.
- [ ] A scoreboard or menu plugin showing `%damageindicator_status%` switches between ON and OFF with `/di`.
- [ ] Delete the `- PLAYER` line under `blacklisted-entities`, `/di reload`: hitting another player shows a
      number to everyone but the player who was hit. With `hide-own-indicators: false`, they see it too.
- [ ] Set `number-format: double` and `decimal-places: 2`, `/di reload`: numbers show at most 2 decimals,
      without trailing zeros (`2.5`, not `2.50`).

## Pull requests

- Run `./gradlew build` (unit tests) and the smoke test for at least one Paper and one Spigot server.
- Only use API that exists in Spigot 1.19.4. If you need something newer or Paper-only, look it up at
  runtime the way the `platform` classes do.

## Releasing (maintainers)

1. Bump `version` in `gradle.properties` and add the changes to `CHANGELOG.md`.
2. Commit and push, and wait for the `Build` workflow to pass.
3. Create a GitHub release tagged `v<version>` (for example `v1.1.0`), with the changes as its description.

The `Publish` workflow then checks the tag matches the version, builds the jar, uploads it to
[Modrinth](https://modrinth.com/plugin/damageindicator) and
[Hangar](https://hangar.papermc.io/PunJarrr/DamageIndicator_) with the release description as the
changelog, and attaches it to the GitHub release.

**SpigotMC has no upload API**, so upload the jar there by hand: on
[the resource page](https://www.spigotmc.org/resources/135211/), choose *Post Resource Update*, attach
the jar from the GitHub release, and paste the changelog.

The workflow needs two repository secrets (Settings → Secrets and variables → Actions):

- `MODRINTH_TOKEN`: a [Modrinth personal access token](https://modrinth.com/settings/pats) with the
  *Create versions* scope.
- `HANGAR_API_TOKEN`: a [Hangar API key](https://hangar.papermc.io/auth/settings/api-keys) with the
  *create_version* permission.

To check the Modrinth upload settings locally without uploading anything, run `./gradlew publishMods`
with no tokens set; it does a dry run.
