---
title: LuckPerms track
description: Where the rank order comes from, and how a purchase is written.
---

RexRankMenu does not keep its own ladder. Each time the menu opens it loads `settings.track` and the groups on that track.

## Track order

LuckPerms stores a track from the lowest group to the highest. That order is the purchase order when `ranks.yml` `order` is empty.

```
/lp createtrack ranks
/lp track ranks append default
/lp track ranks append vip
/lp track ranks append mvp
```

`settings.track` is the track name. In this example it is `ranks`.

The player can buy the first group in that list which they do not already have. If they already have the last group, the menu sells nothing, even when a lower group is missing. Giving `mvp` directly from LuckPerms does not leave `vip` for sale afterwards.

## Parents

A purchase adds the next group as a parent. The previous group stays. Remove the old parent yourself if the track should keep only one.

The plugin asks LuckPerms to save that parent, then loads the user again. If the group is not present on that read, the same amount is sent back and the player gets `messages.failed`.

Money is withdrawn only after the confirm click (`settings.confirm` true) or the rank click (`settings.confirm` false), and only after a fresh read still says that group is next.

## Server context

`settings.server-context` is the value of the LuckPerms `server` context.

Leave it empty to read global parents and to add the parent with an empty context set.

Set a server name to read inherited groups in that context and to add `server=<name>` on the new parent. A global parent still counts in that context. A parent stored for a different server does not.

The value has to match the context LuckPerms already uses. RexRankMenu does not lowercase it.
