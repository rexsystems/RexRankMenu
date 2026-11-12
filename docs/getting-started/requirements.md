---
title: Requirements
description: What the server needs before RexRankMenu will enable.
sidebar:
  order: 2
---

- Java 21
- Paper, Purpur, or Folia 1.21 or newer
- LuckPerms

Vault is required only for a currency with `type: vault`. PlaceholderAPI is required only when a currency reads a placeholder, or when a line in the menu uses a `%placeholder%`.

The plugin declares LuckPerms, Vault, and PlaceholderAPI as soft dependencies. The server will still load the jar when LuckPerms is absent. RexRankMenu then disables itself during startup.
