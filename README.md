# RexRankMenu

Buy the next group on a LuckPerms track from a menu. The track is read when the menu opens. `ranks.yml` sets the price, material, slot, name, and lore for each group.

## Requirements

- Java 21
- Paper, Purpur, or Folia 1.21+
- LuckPerms
- Vault, when a currency uses `type: vault`
- PlaceholderAPI, when a currency or a line uses placeholders

## Install

1. Put the jar in `plugins/`
2. Start the server once so the config files are written
3. Edit `plugins/RexRankMenu/config.yml` and `ranks.yml`
4. Run `/rexrankmenu reload`

If LuckPerms is missing, the plugin disables itself and logs `LuckPerms is not installed. Disabling RexRankMenu.`

## Commands

| Command | Permission |
|---|---|
| `/rexrankmenu` | `rexrankmenu.use` |
| `/rexrankmenu open [player]` | `rexrankmenu.use`, or `rexrankmenu.admin` for another player |
| `/rexrankmenu reload` | `rexrankmenu.reload` |
| `/rexrankmenu help` | everyone |

Aliases: `/rrank`, `/rank`

`rexrankmenu.buy` is checked when the purchase runs.

## Documentation

The pages in [docs/](docs/index.md) drop into a Starlight site at `src/content/docs/rexrankmenu/`. Paste `docs/sidebar.mjs` into the site sidebar.

Full docs: https://rexsystems.me/docs/rexrankmenu
