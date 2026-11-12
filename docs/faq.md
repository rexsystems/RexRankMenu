---
title: FAQ
description: Startup, refunds, and the confirm click.
sidebar:
  order: 1
---

## The plugin disables itself on startup

LuckPerms is a soft dependency, so the server still loads the jar when LuckPerms is absent. RexRankMenu then disables itself. The log line is:

`LuckPerms is not installed. Disabling RexRankMenu.`

If the LuckPerms jar is present but not enabled, the line is:

`LuckPerms is not enabled. Disabling RexRankMenu.`

That path does not print a stack trace. Install LuckPerms, or enable it, and restart.

## The money came back

The purchase withdraws on the confirm click, asks LuckPerms to add the parent, then loads the user again. If that group is not present, RexRankMenu deposits the same amount and sends `messages.failed`. The rank is unchanged.

A Vault refund uses the same economy provider. A placeholder currency runs `give-command`. If that refund command fails, the console logs a severe line with the player, the amount, and the currency so it can be returned by hand.

## The first click did not buy the rank

`settings.confirm` defaults to true. The first click on the next rank only selects it and sends `messages.selected`. The purchase runs when the player clicks the confirm slot (`settings.gui.confirm-slot`, default 49).

Set `settings.confirm` to false to buy on the rank click. Already owned ranks send `messages.already-owned` and do not charge. A later rank sends `messages.not-next`. A short balance sends `messages.not-enough` and does not change LuckPerms or the balance.
