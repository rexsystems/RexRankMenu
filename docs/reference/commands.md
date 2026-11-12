---
title: Commands
description: RexRankMenu commands and who can run them.
sidebar:
  order: 1
---

| Command | Description | Permission |
|---|---|---|
| `/rexrankmenu` | Open your menu | `rexrankmenu.use` |
| `/rexrankmenu open` | Open your menu | `rexrankmenu.use` |
| `/rexrankmenu open <player>` | Open the menu for that online player | `rexrankmenu.admin` |
| `/rexrankmenu reload` | Reload `config.yml` and `ranks.yml`, and close open rank menus | `rexrankmenu.reload` |
| `/rexrankmenu help` | Show the help lines | everyone |

Aliases: `/rrank`, `/rank`

An unknown subcommand shows help. Reload closes any open rank menu so the next open uses the new files. A purchase that has already taken money still finishes.
