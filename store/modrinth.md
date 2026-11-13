# <p align="center">DamageIndicator</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg" alt="paper">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg" alt="purpur">
  <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg" alt="spigot">
  <br>
  <a href="https://modrinth.com/plugin/damageindicator">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/modrinth_vector.svg" alt="modrinth">
  </a>
  <a href="https://hangar.papermc.io/PunJarrr/DamageIndicator_">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/hangar_vector.svg" alt="hangar">
  </a>
  <a href="https://www.spigotmc.org/resources/135211/">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/spigot_vector.svg" alt="spigot">
  </a>
  <a href="https://github.com/PunJarrr/DamageIndicator">
    <img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy-minimal/available/github_vector.svg" alt="github">
  </a>
</p>

##

Ever wondered exactly how hard that sword hit? **DamageIndicator** shows the damage floating above the mob, with the health it has left.

## ✨ Features

- **Damage** above the mob's head, like `-5❤ [15❤]`
- **Healing** in another colour, like `+4❤ [19❤]`
- **Your own text** with colours, gradients and formatting ([MiniMessage](https://docs.papermc.io/adventure/minimessage/format/)), and the placeholders `{damage}`, `{heal}`, `{current}`, `{max}`
- **Adjustable:** how long the text stays, its size, where it appears, whole numbers or decimals
- **Blacklist** for mobs that shouldn't show numbers (players by default)
- **Respects protection:** no numbers when WorldGuard or another plugin cancels the damage
- **Paper, Purpur, Spigot and Folia**, Minecraft **1.19.4 – 26.3**, one jar for all of them
- **Lightweight:** one short-lived text entity per hit, never saved with the world

<details>
<summary>📸 Showcase</summary>

- **Damage**

  ![Damage](https://cdn.modrinth.com/data/7hpegiHp/images/ed7e43ff2cf54f159a4db607153bb3fb66640668.png)

- **Heal**

  ![Heal](https://cdn.modrinth.com/data/7hpegiHp/images/063e724d40e71e38deeb67754709b1a6c8701e51.png)
</details>

## 📦 Supported Versions

| Server | Minecraft |
|---|---|
| Paper | 1.19.4 – 26.3 |
| Purpur | 1.19.4 – 26.3 |
| Spigot | 1.19.4 – 26.3 |
| Folia | 1.19.4 – 26.3 |

## 📄 Installation

1. Download the jar (the same file works on every server and version above)
2. Put it in your server's `plugins` folder
3. Restart the server

Only the server needs it. Players don't install anything.

## ⚙️ Configuration

- Change the text, how long it stays, its size and position, and the blacklist in `config.yml`.
- The text uses [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/). Placeholders: `{damage}`, `{heal}`, `{current}`, `{max}`.
- Command messages are in `translations/<language>.properties`.
- Run `/di reload` after changing anything. Updating from an older version? Your `config.yml` is updated automatically, with your settings kept.

<details>
<summary>📝 config.yml</summary>

```yaml
############################
#     Damage Indicator     #
############################

# After changing anything, run /di reload.

# Plugin settings
settings:
  # The language file to use for command messages, from the translations folder (e.g. en_US).
  language: en_US

  # Customize the damage and heal text displayed above mobs when they are damaged or healed.
  # Uses MiniMessage: https://docs.papermc.io/adventure/minimessage/format/
  text-format:
    # Placeholders:
    # {damage} - The amount of damage dealt to the mob.
    # {heal} - The amount of healing received by the mob.
    # {current} - The mob's current health after the damage or heal is applied.
    # {max} - The mob's maximum health.

    # The text to display when a mob is damaged.
    damage: "<red>-{damage}❤ <aqua>[{current}❤]"
    # The text to display when a mob is healed.
    heal: "<green>+{heal}❤ <aqua>[{current}❤]"

  # The duration (in ticks) that the text will be displayed (1 second = 20 ticks).
  display-duration: 10

  # The format used for numbers in the displayed text.
  # Options: int (whole numbers, rounded down, e.g. 5), double (decimals, e.g. 5.25)
  number-format: int

  # Settings for the text display entity.
  text-display:
    # The scale of the text display (1.0 = default size).
    scale: 1.0
    # How the text turns to face the player.
    # Options: CENTER (always faces the player), VERTICAL (turns sideways only),
    #          HORIZONTAL (tilts up and down only), FIXED (never turns)
    billboard: CENTER

  # The location where the text will be displayed above the mob's head.
  text-location:
    # The vertical offset (in blocks) from the mob's head where the text will be displayed.
    vertical-offset: 0.5
    # The radius (in blocks) around the mob's head where the text will be displayed,
    # so numbers from quick hits don't stack on top of each other.
    radius: 0.5

  # Entity types that never show damage or heal indicators (e.g. PLAYER, VILLAGER, WOLF).
  # Names: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html
  # Armor stands never show indicators.
  blacklisted-entities:
    - PLAYER

# The version of this file. DamageIndicator uses it to update older config files automatically
# (your settings are kept, and the old file is saved in the backup folder). Don't change it.
config-version: 1.1.0
```
</details>

## 📚 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/di reload` | `damageindicator.reload` | Reload the config and messages (operators by default). |

<details>
<summary>❓ FAQ</summary>

**I'm updating from an older version. Do I lose my settings?**
No. Old config files are updated automatically, your settings are kept, and a backup is saved in `plugins/DamageIndicator/backup/`.

**Does it work on Spigot and Folia?**
Yes, from 1.1.0. Paper and Purpur work too.

**Can I see numbers when players get hurt?**
Yes. Remove `PLAYER` from `blacklisted-entities` and run `/di reload`.

**Will it leave floating text behind if the server crashes?**
No. From 1.1.0, the text is never saved with the world, so it can't get stuck.

**Does it show numbers in protected areas?**
From 1.1.0, no. When WorldGuard or another plugin cancels the damage or healing, nothing is shown.
</details>

## 💬 Feedback

Found a bug or have an idea? Open an issue on [GitHub](https://github.com/PunJarrr/DamageIndicator/issues).

## ❤️ Credits

Bundles [Adventure and MiniMessage](https://github.com/KyoriPowered/adventure) by KyoriPowered (MIT License).
