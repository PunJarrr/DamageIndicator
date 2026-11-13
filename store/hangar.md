# DamageIndicator

<p></p>

<img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/paper_vector.svg" alt="paper">
<img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/purpur_vector.svg" alt="purpur">
<img src="https://raw.githubusercontent.com/intergrav/devins-badges/0a3449fd26bf1375d2c5c26f096c8f30aa358766/assets/cozy/supported/spigot_vector.svg" alt="spigot">
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
- Run `/di reload` after changing anything.

<details>
<summary>📝 config.yml</summary>

```yaml
############################
#     Damage Indicator     #
############################

# Plugin settings
settings:
  # The language file to use from the translations folder (e.g. en_US).
  language: en_US

  # Customize the damage and heal text displayed above mobs when they are damaged or healed.
  text-format:
    # Placeholders:
    # {damage} - The amount of damage dealt to the mob.
    # {heal} - The amount of healing received by the mob.
    # {current} - The mob's current health after the damage or heal is applied.
    # {max} - The mob's maximum health.

    # The text to display when a mob is damaged. Use {damage} to show the damage amount.
    damage: "<red>-{damage}❤<aqua>[{current}❤]"
    # The text to display when a mob is healed. Use {heal} to show the heal amount.
    heal: "<green>+{heal}❤<aqua>[{current}❤]"

  # The duration (in ticks) that the text will be displayed (1 second = 20 ticks).
  display-duration: 10

  # The format used for numbers in the displayed text.
  # Options: int (e.g. 5), double (e.g. 5.25)
  number-format: int

  # Settings for the text display entity.
  text-display:
    # The scale of the text display (1.0 = default size).
    scale: 1.0
    # The billboard mode of the text display.
    # Options: CENTER (always faces the player), VERTICAL (rotates only on the Y-axis)
    billboard: CENTER

  # The location where the text will be displayed above the mob's head.
  text-location:
    # The vertical offset (in blocks) from the mob's head where the text will be displayed.
    vertical-offset: 0.5
    # The radius (in blocks) around the mob's head where the text will be displayed.
    radius: 0.5

  # The list of entities that are blacklisted from showing damage or heal indicators.
  blacklisted-entities:
    - PLAYER

# Leave this option alone unless you know what you're doing.
# Changing this value may cause unexpected issues!
config-version: 1.0.0
```
</details>

## 📚 Commands & Permissions

| Command | Permission | Description |
|---|---|---|
| `/di reload` | `damageindicator.reload` | Reload the config and messages (operators by default). |

<details>
<summary>❓ FAQ</summary>

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
