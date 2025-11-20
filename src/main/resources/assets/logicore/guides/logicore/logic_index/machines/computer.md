---
navigation:
  title: "Computer"
  icon: "logicore:computer"
  parent: logicore:machines_category.md
item_ids:
  - logicore:computer
---

# Computer

## Computer

<ItemImage id="logicore:computer" />

The personal workstation of the LogiCore network. While it lacks the raw parallel processing power of a [Server Rack](./server_rack.md), the Computer is a reliable entry-level machine for generating [Cycles](../architecture/cycles.md).

## System Requirements

To function, the Computer requires a valid [Processor Unit](../components/processor_unit.md) installed in its chassis and a connection to a power source via [Data Cable](../components/data_cable.md).

**Performance:**
Expect lower cycle generation rates compared to industrial hardware.

## Redstone Control

The Computer features a standard hardware interrupt port.

Applying a **redstone signal** will force the system into standby mode. While in standby, the machine stops generating [Cycles](../architecture/cycles.md) but also halts all energy consumption.

A compact chassis capable of housing standard processor units.

<Recipe id="logicore:computer" />

