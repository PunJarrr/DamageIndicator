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

Test servers are Paper 1.19.4 – 26.3, plus Purpur, Folia and Spigot at the oldest and newest versions.
Each runs in `run/<platform>-<version>/`. Spigot is built with Spigot's BuildTools the first time, which
takes a few minutes.

## Smoke test

`scripts/smoke-test.sh` boots each test server and, over RCON (the server console), checks that:

1. the plugin enables, using the right text path (Paper's Adventure, or colour codes on Spigot) and the
   right scheduler (Folia's entity scheduler on Folia)
2. `/di reload` answers "Reloaded configuration successfully!"
3. hitting a blacklisted pig with `/damage` shows no text
4. hitting a cow for 3 damage shows `-3❤[7❤]`, and healing it with Instant Health shows `+4❤[10❤]`
5. the text is gone again after `display-duration`
6. the server log has no errors (harmless known lines are listed in `scripts/smoke-test-ignore.txt`).

For the test, `display-duration` is set to 100 ticks so there's time to look, and `PIG` is added to the
blacklist. Everything else uses the default config.

Running it accepts the [Minecraft EULA](https://aka.ms/MinecraftEULA) for the test servers. It needs
Python 3.

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
      `/di reload`: it only turns sideways.
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
