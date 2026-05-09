---
navigation:
  title: "Commands"
  icon: "minecraft:command_block"
  parent: logicore:commands_category.md
---

# Commands

## /logicore

All mod commands are available under the `/logicore` root command. These commands require **op level 2**
permissions to execute.

Subcommands are organized into two groups: `network` and `knowledge`.

## /logicore network

**`list`** -- Lists all active computation networks and their unique IDs. Useful for debugging and
monitoring network topology.

**`clear`** -- Removes all registered computation networks from the world. Use with caution -- this
cannot be undone.

## /logicore knowledge

**`list`** -- Displays the names of all items you have researched so far in chat.

**`clear`** -- Wipes your entire research knowledge database. All items will need to be researched
again.

**`show`** -- Opens the Knowledge Base UI -- a scrollable 9x9 grid showing all researched items with
a search bar to quickly find specific entries.
