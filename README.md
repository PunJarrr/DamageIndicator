<div align="center">

<img src="https://cdn.modrinth.com/data/7hpegiHp/341758c878838872789376d2cae421ba1ccbd2ff_96.webp" alt="DamageIndicator icon" width="96">

# DamageIndicator

**Floating damage and heal numbers above mobs.**

[![Available on Modrinth](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/modrinth_vector.svg)](https://modrinth.com/plugin/damageindicator)
[![Available on Hangar](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/hangar_vector.svg)](https://hangar.papermc.io/PunJarrr/DamageIndicator_)
[![Available on Spigot](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/available/spigot_vector.svg)](https://www.spigotmc.org/resources/135211/)

![Supports Paper](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg)
![Supports Purpur](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg)
![Supports Spigot](https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg)

<img src="https://cdn.modrinth.com/data/7hpegiHp/images/ed7e43ff2cf54f159a4db607153bb3fb66640668.png" alt="A damage number floating above a mob">

</div>

## About

DamageIndicator is a small server plugin. When a mob takes damage, the damage floats above its head for a
moment, with its health left. When it heals, the amount healed shows in another colour.

- **Damage**, for example `-5❤ [15❤]`, in red.
- **Healing**, for example `+4❤ [19❤]`, in green.
- **Your own text**: colours, gradients and formatting with [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/),
  and the placeholders `{damage}`, `{heal}`, `{current}` and `{max}`.
- **Adjustable**: how long the text stays, its size, where it appears, whole numbers or decimals.
- **Blacklist** for mobs that shouldn't show numbers (players by default).
- **Respects protection**: no numbers when another plugin cancels the damage, such as WorldGuard regions.
- **Lightweight**: one short-lived text entity per hit, never saved with the world.

## Download

Get it from [Modrinth](https://modrinth.com/plugin/damageindicator),
[Hangar](https://hangar.papermc.io/PunJarrr/DamageIndicator_),
[SpigotMC](https://www.spigotmc.org/resources/135211/) or
[GitHub releases](https://github.com/PunJarrr/DamageIndicator/releases).
One jar works on every supported server and version. Put it in your server's `plugins` folder and restart.

| Server | Minecraft |
|---|---|
| Paper | 1.19.4 – 26.3 |
| Purpur | 1.19.4 – 26.3 |
| Spigot | 1.19.4 – 26.3 |
| Folia | 1.19.4 – 26.3 |

Only the server needs the plugin. Players don't install anything. 1.19.4 is the oldest version because
that's when Minecraft added the floating text entities the plugin uses.

## Configuration

`plugins/DamageIndicator/config.yml`:

| Option | Default | What it does |
|---|---|---|
| `settings.language` | `en_US` | Which file in `translations/` to use for command messages. |
| `settings.text-format.damage` | `<red>-{damage}❤ <aqua>[{current}❤]` | The text shown when a mob is damaged. |
| `settings.text-format.heal` | `<green>+{heal}❤ <aqua>[{current}❤]` | The text shown when a mob is healed. |
| `settings.display-duration` | `10` | How long the text stays, in ticks (20 ticks = 1 second). |
| `settings.number-format` | `int` | `int` for whole numbers, rounded down (`5`), `double` for decimals (`5.25`). |
| `settings.text-display.scale` | `1.0` | Text size (1.0 = normal). |
| `settings.text-display.billboard` | `CENTER` | `CENTER` always faces the player, `VERTICAL` only turns sideways, `HORIZONTAL` only tilts up and down, `FIXED` never turns. |
| `settings.text-location.vertical-offset` | `0.5` | How far above the mob's head the text appears, in blocks. |
| `settings.text-location.radius` | `0.5` | How far to the side the text may appear, in blocks, so numbers don't stack. |
| `settings.blacklisted-entities` | `[PLAYER]` | [Entity types](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html) that never show numbers. Armor stands never do. |

Placeholders for the text: `{damage}` (damage taken), `{heal}` (health gained), `{current}` (health after
the change) and `{max}` (maximum health).

Command messages are in `plugins/DamageIndicator/translations/<language>.properties`, also in MiniMessage.
Run `/di reload` after changing anything. If a setting has a value the plugin doesn't understand (a typo in
`billboard`, `number-format` or an entity name), the server log says so.

## Commands and permissions

| Command | Permission | Default | What it does |
|---|---|---|---|
| `/di reload` | `damageindicator.reload` | operators | Reloads the config and messages. |

`/damageindicator` works too.

## FAQ

**I'm updating from an older version. Do I lose my settings?**
No. When the plugin starts, it updates an older `config.yml` to the new layout and keeps all your settings.
The old file is saved in `plugins/DamageIndicator/backup/`. If you never changed the damage or heal text,
you get the new default, which has a space before the health (`-5❤ [15❤]`).

**Does it work on Spigot and Folia?**
Yes, from version 1.1.0. Paper and Purpur are supported too.

**Can I see numbers when players get hurt?**
Yes. Remove `PLAYER` from `blacklisted-entities` and run `/di reload`.

**Will it leave floating text behind if the server crashes?**
No. From 1.1.0, the text is never saved with the world, so it can't get stuck. If an older version left
text behind, stand next to it and run `/kill @e[type=text_display,distance=..3]` to remove it (this
also removes any other floating text within 3 blocks, such as holograms from other plugins).

**Does it show numbers in protected areas?**
From 1.1.0, no. When WorldGuard or another plugin cancels the damage or healing, nothing is shown.

## Contributing

Bug reports and ideas are welcome on the
[issue tracker](https://github.com/PunJarrr/DamageIndicator/issues). To build the plugin or change its
code, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Credits

DamageIndicator bundles [Adventure and MiniMessage](https://github.com/KyoriPowered/adventure) by
KyoriPowered (MIT License). See [credits.txt](credits.txt), which is also included in the jar.
Badges by [devins-badges](https://github.com/intergrav/devins-badges).

## License

[MIT](LICENSE)
