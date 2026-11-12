---
title: Messages
description: Chat lines sent by RexRankMenu.
sidebar:
  order: 3
---

Every string under `messages` accepts MiniMessage, `&` codes, `&#RRGGBB`, and `#RRGGBB`. Values placed into `{placeholders}` are inserted as plain text.

| Placeholder | Value |
|---|---|
| `{player}` | Account name |
| `{group}` | LuckPerms group name |
| `{rank}` | Display name with formatting removed |
| `{price}` | Price, trailing zeroes removed |
| `{amount}` | Same number as `{price}` |
| `{currency}` | Currency id |
| `{next}` | Next group name |
| `{track}` | Track name |
| `{status}` | Plain status word, used in item lore |

| Key | When it is sent |
|---|---|
| `no-permission` | The sender lacks the permission for that command. |
| `players-only` | Console tried to open its own menu. |
| `player-not-found` | `/rexrankmenu open` could not find that player. |
| `reloaded` | Reload finished. |
| `track-missing` | `settings.track` is not a LuckPerms track. |
| `track-empty` | The track has no groups. |
| `luckperms-failed` | LuckPerms failed while reading the user. Nothing is charged. |
| `already-owned` | The clicked group is already on the player. |
| `not-next` | The clicked group is not the next one on the ladder. |
| `not-enough` | The balance is below the price. Nothing is changed. |
| `purchased` | LuckPerms has the group after the purchase. |
| `failed` | The group was not present when the user was read again. The payment is returned. |
| `highest` | The player already has the last group on the ladder. |
| `no-buy` | The player lacks `rexrankmenu.buy`. |
| `currency-missing` | Vault, the economy provider, or the placeholder command is not available. Nothing is charged. |
| `busy` | A purchase for that player is already running. |
| `selected` | Confirm is on, and the next rank was selected. |
| `select-first` | The confirm slot was clicked with nothing selected. |
| `status-owned` | Plain status for a group the player has. |
| `status-next` | Plain status for the group they can buy. |
| `status-locked` | Plain status for a later group. |

`help` is a list of lines sent by `/rexrankmenu help`.
