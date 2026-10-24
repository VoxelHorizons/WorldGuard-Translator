WorldGuard-Translator
=

<img src="logo.png" alt="Logo" height="180px" align="right" />

The easiest, fastest, and best way to translate and customize messages for the <b><a href="https://modrinth.com/plugin/worldguard">WorldGuard</a></b> plugin.

![bStats Servers](https://img.shields.io/bstats/servers/31595?style=for-the-badge)
![bStats Players](https://img.shields.io/bstats/players/31595?style=for-the-badge)
![GitHub Repo stars](https://img.shields.io/github/stars/SuperCHIROK1/WorldGuard-Translator?style=for-the-badge)

---

## Optional PlaceholderAPI and colour codes

The VoxelHorizons fork supports **optional** PlaceholderAPI expansion in translated
messages. Add PlaceholderAPI to the server to activate this integration; no
PlaceholderAPI expansion or separate configuration is needed for plain messages.
Disable it when desired:

```yaml
# plugins/WorldGuard-Translator/config.yml
placeholderapi:
  enabled: true
```

For example, in `plugins/WorldGuard-Translator/translations/en.yml`:

```yaml
components:
  "PvP": "&f%voxelcore_font_pvp% &cYou cannot PVP here. &fMaybe visit the &ePVP arena?"
```

When WorldGuard substitutes `%what%` with the `PvP` component, the plugin expands
the recipient's PlaceholderAPI values (including VoxelCore font glyphs), then
applies colour formatting. A missing PlaceholderAPI plugin leaves the original
`%placeholder%` text unchanged and does not prevent WorldGuard-Translator
from starting.

Both the standard legacy codes (`&f`, `&c`, `&l`, `&r`,
`&0`–`&9`) and hex colours (`&#ff5400`, `&<#ff5400>`) are supported,
including colours returned by a placeholder expansion. Formatting is applied
after substituted components, fixing the previous `&f` rendering problem.
MiniMessage formatting continues to work when `serializer: MINI_MESSAGE` is
configured.

**Message coverage:** Denied-action chat/actionbar messages and translated
`/wgt` command responses use per-recipient expansion. The fork also attempts
to intercept WorldGuard's entry/exit movement denials and direct teleport
denials to apply the same per-player rendering. These hooks patch WorldGuard
methods in memory and depend on WorldGuard's internal method signatures. If
a hook cannot be installed on your WorldGuard version, a warning is logged and
normal WorldGuard behaviour remains active for that path. Messages explicitly
configured on WorldGuard regions still originate from WorldGuard and are
rendered when they flow through one of the hooked paths.

## Features
- Extremely fast performance and lightweight
- MiniMessage formatting support
- Modify messages directly in-game
- Translation of English action keys (e.g., `break that block`)
- Compatibility with any WorldGuard version
- Automatic downloading of pre-made translations

## How It Works
WorldGuard-Translator modifies WorldGuard's code directly in RAM during server startup or plugin reload.
The plugin intercepts the original deny messages and dynamically replaces English technical strings with your translated equivalents.

## Pre-made Translations
Pre-made community translations for WorldGuard-Translator can be found in the [/translations/](https://github.com/SuperCHIROK1/WorldGuard-Translator/tree/main/translations) folder.

By the way, you can contribute to the development of the project, [learn more here](https://github.com/SuperCHIROK1/WorldGuard-Translator/tree/main/CONTRIBUTING.md).

<p align="center">
  <img src="demo.png" alt="demo" width="100%" />
</p>

<p align="center"><b>Developed by SuperCHIROK1 • 2026</b></p>
<p align="center"><a href="https://dsc.gg/amazingplugins">Join our Discord server</a></p>
