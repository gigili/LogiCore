---
navigation:
  title: "Repair station"
  icon: "logicore:repair_station"
  parent: logicore:machines_category.md
item_ids:
  - logicore:repair_station
---

# Repair station

## Repair station

<ItemImage id="logicore:repair_station" />

The Repair station restores durability on damaged tools and armor by consuming [Cycles](../architecture/cycles.md).

Insert a damaged item and the station will request cycles from adjacent [Data Cables](../components/data_cable.md). Each
point of durability costs cycles, so higher-cost items take longer and consume more. It holds one item at a time.

<Recipe id="logicore:repair_station" />
