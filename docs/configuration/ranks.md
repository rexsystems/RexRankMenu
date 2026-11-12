---
title: Ranks
description: How ranks.yml dresses the LuckPerms track.
sidebar:
  order: 2
---

`ranks.yml` does not choose which groups exist. LuckPerms does, from `settings.track`, at the moment the menu opens.

```yaml
order: []

ranks:
  vip:
    price: 10000
    currency: vault
    name: "<gold>VIP"
    lore:
      - "<gray>Price: <white>{price} {currency}"
      - "<gray>{status}"
    slot: 20
    material: GOLD_INGOT
    custom-model-data: 1001
```

| Key | Meaning |
|---|---|
| `price` | Amount taken on the confirm click. `0` adds the parent and does not charge. A missing price is `0`. |
| `currency` | Id from `currencies` in `config.yml`. |
| `name` | MiniMessage item name. Blank uses the group name. |
| `lore` | MiniMessage lines. Omit the key for a price line and a status line. `lore: []` shows no lore. |
| `slot` | Inventory slot. Omit it to take the next free slot. |
| `material` | Bukkit material name. An unknown name uses `settings.default-material`. |
| `custom-model-data` | Set only when the key is present. |

A group that has no section still appears. Its item is the default material, its name is the group name, and its price is 0.

`order` is optional. Leave it empty to follow the LuckPerms track from lowest to highest. When it lists groups, those names that are actually on the track come first, in the listed order. Track groups that were left out are appended in track order. Names that are not on the track are ignored, so a new LuckPerms group still shows up without a rewrite of this file.

The confirm slot is reserved while `settings.confirm` is true. A rank that asks for that slot is moved to another free slot.

Item lines can use `{group}`, `{rank}`, `{price}`, `{currency}`, `{status}`, `{player}`, `{track}`, `{next}`, and `{amount}`. `{rank}` is the display name with formatting removed. `{status}` is the plain word from `messages.status-owned`, `messages.status-next`, or `messages.status-locked`. PlaceholderAPI placeholders are resolved for the player who is looking at the menu.
