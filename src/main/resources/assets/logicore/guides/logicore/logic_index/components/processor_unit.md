---
navigation:
  title: "Processor Unit"
  icon: "logicore:processor_unit"
  parent: logicore:components_category.md
item_ids:
  - logicore:processor_unit
---

# Processor Unit

## Processor Unit

<ItemImage id="logicore:processor_unit" />

The silicon heart of your operations. The Processor Unit is the primary component responsible for generating [Cycles](../architecture/cycles.md).

It must be installed in [Server Racks](../machines/server_rack.md) or [Computers](../machines/computer.md). Without a processor, these machines are inert—producing nothing even if connected to power.

## Operational Control

Machines equipped with a Processor Unit are **redstone sensitive**.

Applying a redstone signal acts as a hardware interrupt. The machine will immediately cease all operations, halting [Cycle](../architecture/cycles.md) production and stopping energy consumption. This is useful for automating power efficiency.

Precision engineering is required to turn raw silicon into a functional logic gate array.

<Recipe id="logicore:processor_unit" />

