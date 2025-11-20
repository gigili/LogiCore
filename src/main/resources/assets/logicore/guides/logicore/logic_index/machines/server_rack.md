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

To operate, the rack requires a [Processor Unit](../components/processor_unit.md) installed in its bay and a connection to the network via [Data Cable](../components/data_cable.md).

Due to its high throughput, ensure your power generation can sustain its energy demands.

## Redstone Control

Like other LogiCore machines, the Server Rack is **redstone Sensitive**.

Applying a steady Redstone signal will trigger a hardware interrupt. This pauses all calculation threads, stopping [Cycle](../architecture/cycles.md) production and cutting power draw to zero—ideal for demand-based automation.

An industrial-grade housing unit designed for maximum airflow and component density.

<Recipe id="logicore:server_rack" />

