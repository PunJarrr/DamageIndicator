# Changelog

## 1.1.0

### Added
- Spigot support. DamageIndicator now works on Spigot as well as Paper and Purpur.
- Folia support.
- Minecraft 1.19.4 – 1.21.2 and 26.2 – 26.3 support. One jar now covers 1.19.4 – 26.3.

### Fixed
- Damage and heal text can no longer get stuck in the world. Before, if the server stopped or the chunk
  unloaded in the half second the text was showing, it stayed there for good.
- No text when another plugin (such as WorldGuard) cancels the damage or healing.
- The numbers now match the damage after every other plugin has changed it.

## 1.0.0

- Floating damage and heal numbers above mobs for Paper 1.21.3 – 26.1.2.
