---
navigation:
  title: "Drone"
  icon: "logicore:drone"
  parent: logicore:machines_category.md
item_ids:
  - logicore:drone
---

# Drone

## Drone

<ItemImage id="logicore:drone" />

The Drone is an autonomous automated assistant. Once deployed, it bonds with its owner and performs various tasks
using [Cycles](../architecture/cycles.md). It requires a [Drone Bay](./drone_bay.md) nearby to recharge.

## Behavior & Upkeep

The Drone will follow its owner and automatically heal them when injured, consuming
stored [Cycles](../architecture/cycles.md).

When its internal storage runs low, it will automatically return to its assigned [Drone Bay](./drone_bay.md) to recharge
before resuming duties.

## Deployment

To activate a Drone, right-click it onto a block near a [Drone Bay](./drone_bay.md).

**Customization:**
Renaming the Drone Item in an Anvil before placing it will give the entity a permanent name tag.

Combines advanced mobility with a processor unit for autonomous logic.

<Recipe id="logicore:drone" />

<GameScene zoom={2} interactive={true}>
    <Entity id="logicore:drone" />
</GameScene>
