---
navigation:
  title: "Recycler"
  icon: "logicore:recycler"
  parent: logicore:machines_category.md
item_ids:
  - logicore:recycler
---

# Recycler

## Recycler

<ItemImage id="logicore:recycler" />

The **Recycler** is a machine that converts physical items back into [Cycles](../architecture/cycles.md). It allows you
to reclaim computational value from items you no longer need, provided you have
already [researched](reserach_station.md) them.

## Operation

The Recycler requires **FE** (Forge Energy) to operate. When provided with power and an item that has a
known [Cycle Value](../architecture/cycles.md), it will break down the item and generate Cycles.

Only items that have been fully researched can be recycled by this machine.

## Cycle Distribution

By default, generated Cycles are stored in the Recycler's internal buffer and can be distributed to the network
via [Data Cables](../components/data_cable.md).

If a [Cloud Interface](cloud_interface.md) is connected (with its back to the Recycler), it will automatically upload
the Cycles directly to your cloud storage.

<Recipe id="logicore:recycler" />
