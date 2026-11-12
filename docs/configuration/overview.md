---
title: Settings
description: Track, confirm slot, server context, and currencies.
sidebar:
  order: 1
---

`config.yml` holds the track, the menu frame, currencies, and messages. Group prices live in [`ranks.yml`](/rexrankmenu/configuration/ranks/).

## Track

`settings.track` is the LuckPerms track name. The ordered groups on that track are loaded when the menu opens.

`settings.server-context` is the LuckPerms `server` context.

- An empty value reads global parents and adds the new parent with no context.
- A server name, such as `survival`, reads groups in that context and adds the parent with `server=survival`. Global parents still count as owned. A parent that exists only on another server does not.

## Confirm

`settings.confirm` defaults to true.

The player clicks the next rank, then clicks `settings.gui.confirm-slot`. Money is taken on that second click. Set `confirm` to false to buy on the rank click. The confirm slot is then a normal slot.

Clicks in the player's inventory are not cancelled. Clicks inside the rank menu are cancelled, so the items stay in place. A drag that touches the menu is cancelled. A drag that stays in the player's inventory is not.

## Currencies

Each entry under `currencies` has an id used by `ranks.yml`.

`type: vault` withdraws and deposits through Vault. Vault can be absent at startup. A purchase with that currency stops, sends `messages.currency-missing`, and does not change the player.

`type: placeholder` uses three fields:

| Field | Role |
|---|---|
| `balance-placeholder` | Read on the player. It must resolve to a number. |
| `take-command` | Console command that removes money. |
| `give-command` | Console command that returns money. |

Both commands accept `{player}` and `{amount}`. A leading slash is stripped. `{amount}` is a plain number, with trailing zeroes removed.

```yaml
tokens:
  type: placeholder
  balance-placeholder: "%tokens_balance%"
  give-command: "tokens give {player} {amount}"
  take-command: "tokens take {player} {amount}"
```

The menu GUI frame is `settings.gui`: title, rows (1 through 6), confirm slot, filler, and the confirm item text. Set `filler` to `none` for an empty background. `settings.default-material` is used when a group has no material, or the material name is not an item.
