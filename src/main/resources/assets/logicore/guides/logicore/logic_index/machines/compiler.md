---
navigation:
  title: "Compiler"
  icon: "logicore:compiler"
  parent: logicore:machines_category.md
item_ids:
  - logicore:compiler
---

# Compiler

## Compiler

<ItemImage id="logicore:compiler" />

The Compiler is the bridge between the digital and physical realms. It allows you to materialize items directly from raw
computational power, synthesizing matter from [Cycles](../architecture/cycles.md).

## Operation

Place a 'ghost' template item in the left slot. The Compiler will request [Cycles](../architecture/cycles.md) from the
network to match the item's defined cost.

Once fully charged, the matter is synthesized and the result appears in the output slot.

## Processing Speed

The compilation process is not instantaneous. The time required depends on the complexity (Cycle cost) of the object.

Simple components materialize rapidly, while complex machinery requires a longer, stable connection to compile fully.

A matter-synthesis engine capable of rearranging atomic structures.

<Recipe id="logicore:compiler" />

## Stack upgrade

<ItemImage id="logicore:stack_upgrade" />

The Compiler supports adding [Stack upgrades](../components/stack_upgrade.md) to it in order to increase the number of
items it produces. Each stack upgrades increases it by 4 items, with a total of 16 upgrades it produces 64 items in one
operation.
