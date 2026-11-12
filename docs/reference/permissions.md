---
title: Permissions
description: Permission nodes shipped with RexRankMenu.
sidebar:
  order: 2
---

| Permission | Default | Description |
|---|---|---|
| `rexrankmenu.use` | true | Open your own menu |
| `rexrankmenu.buy` | true | Buy the next rank |
| `rexrankmenu.reload` | op | Reload the files |
| `rexrankmenu.admin` | op | Open the menu for another player |

`rexrankmenu.use` is checked for `/rexrankmenu` and `/rexrankmenu open` with no target. `rexrankmenu.buy` is checked on the purchase click, so a player can be allowed to look and not to buy. Opening the menu for someone else checks `rexrankmenu.admin` on the sender. The target's `rexrankmenu.buy` is what allows the purchase.
