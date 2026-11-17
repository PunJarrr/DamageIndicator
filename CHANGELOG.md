# Changelog

## 1.2.0

### Added
- Players can turn the numbers off or back on for themselves by typing `/di` (or `/di toggle`). The choice is
  saved with the player, so it's remembered across logins and restarts. Other players still see the numbers.
- `damageindicator.toggle` permission for `/di`, given to everyone by default.
- `show-numbers-by-default` in `config.yml`: set it to `false` to start with numbers off, so players turn them
  on with `/di` instead.
- PlaceholderAPI support (optional):
  - `%damageindicator_shown%` (`true`/`false`) and `%damageindicator_status%` (`ON`/`OFF`), whether a player
    sees the numbers.
  - Other plugins' placeholders work in the damage and heal text (filled in for the player who hit the mob)
    and in messages (for the player who ran the command).

### Changed
- `/di` on its own now turns the numbers on or off. Before, it only showed the usage message, and only for
  operators.
- `config.yml` is updated to the new layout automatically, with your settings kept.
- Messages added in new versions show even if your `translations` file is from an older version.

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
