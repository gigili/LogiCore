---
navigation:
  title: "Recycler Enchantment"
  icon: "minecraft:enchanted_book"
  parent: logicore:architecture_category.md
---

# Recycler Enchantment

## Recycler

The **Recycler** is a custom enchantment that can be applied to mining tools, most notably
the [Cycle Pickaxe](logicore:components/cycle_pick.md).

When a block with an assigned **Cycle Value** is mined, it is instantly converted into Cycles and added to your cloud
storage instead of dropping as an item.

## Usage & Mechanics

The Recycler respects **Fortune** and **Looting** enchantments, increasing the cycle yield accordingly.

If you wish to get the block itself, hold **Shift** while mining to bypass the recycling process.

## Recycler Blacklist

Some blocks are protected from the Recycler to prevent accidental destruction of critical infrastructure. This is
managed via the `logicore:recycler_blacklist` block tag.

By default, all LogiCore machines and components are blacklisted.
