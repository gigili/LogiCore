---
navigation:
  title: "Server Rack"
  icon: "logicore:server_rack"
  parent: logicore:machines_category.md
item_ids:
  - logicore:server_rack
---

# Server Rack

## Server Rack

<ItemImage id="logicore:server_rack" />

The heavy lifter of the LogiCore network. While the [Computer](./computer.md) is suitable for small tasks, the Server Rack is designed for high-density parallel processing, generating significantly more [Cycles](../architecture/cycles.md) per tick.

## Configuration

The Server Rack accepts up to 9 [Servers](./server.md) in its hot-swappable bays. Each Server must be populated
with [Processor Units](../components/processor_unit.md) before insertion.

Right-click the rack with a Server to insert it. Right-click the rack empty-handed to open its GUI, where Servers can be
managed manually. The rack requires a connection to the network via [Data Cable](../components/data_cable.md).

Due to its high throughput, ensure your power generation can sustain its energy demands.

## Datacenter Boost

When placed inside a formed [Datacenter](../architecture/datacenter.md) multiblock structure, the Server Rack receives a
significant boost to cycle production. The enclosed environment optimizes cooling and power delivery for peak
performance.

An industrial-grade housing unit designed for maximum airflow and component density.

<Recipe id="logicore:server_rack" />
