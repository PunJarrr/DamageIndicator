# Changelog

## 1.1.0

### Added
- Spigot support. DamageIndicator now works on Spigot as well as Paper and Purpur.
- Folia support.
- Minecraft 1.19.4 – 1.21.2 and 26.2 – 26.3 support. One jar now covers 1.19.4 – 26.3.
- `billboard` also accepts `HORIZONTAL` (only tilts up and down) and `FIXED` (never turns).
- The server log warns about settings it doesn't understand, such as a typo in `billboard`,
  `number-format` or an entity name in `blacklisted-entities`.

### Changed
- `config.yml` is updated automatically when the plugin starts. Your settings are kept, new options and
  comments are added, and the old file is saved in `plugins/DamageIndicator/backup/`.
- The default damage and heal text has a space before the health: `-5❤ [15❤]`. Configs that still use the
  old default text get the new one; custom text is kept as it is.

### Fixed
- Damage and heal text can no longer get stuck in the world. Before, if the server stopped or the chunk
  unloaded in the half second the text was showing, it stayed there for good.
- No text when another plugin (such as WorldGuard) cancels the damage or healing.
- The numbers now match the damage after every other plugin has changed it.
- Translation files are read as UTF-8, so messages in any language (and symbols like ❤) show correctly.

## 1.0.0

- Floating damage and heal numbers above mobs for Paper 1.21.3 – 26.1.2.
