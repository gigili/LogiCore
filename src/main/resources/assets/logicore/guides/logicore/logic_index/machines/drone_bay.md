---
navigation:
  title: "Drone Bay"
  icon: "logicore:drone_bay"
  parent: logicore:machines_category.md
item_ids:
  - logicore:drone_bay
---

# Drone Bay

## Drone Bay

<ItemImage id="logicore:drone_bay" />

The Drone Bay serves as a dedicated home base for your [Drones](./drone.md). It acts as a parking spot and a high-speed
charging station for your automated entities.

## Operation

Drones assigned to this bay will automatically return when their internal energy is low. Once docked, the bay transfers
its stored [Cycles](../architecture/cycles.md) to the drone.

The bay must be connected to your network via [Data Cables](../components/data_cable.md) to receive power.

## Pairing & Display

To link a Drone to a specific bay, simply deploy the Drone item on a block adjacent to the Drone Bay.

The holographic display on the front shows the bay's current charge level and the name of the docked drone (if present).

A specialized charging pad with wireless cycle transmission capabilities.

<Recipe id="logicore:drone_bay" />
