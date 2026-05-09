---
navigation:
  title: "Processor units"
  icon: "logicore:processor_unit_basic"
  parent: logicore:components_category.md
item_ids:
  - logicore:processor_unit_basic
  - logicore:processor_unit_advance
  - logicore:processor_unit_ultimate
---

# Processor Unit

## Processor Unit

<ItemImage id="logicore:processor_unit_basic" />

The silicon heart of your operations. The Processor Unit (CPU) is the primary component responsible for
generating [Cycles](../architecture/cycles.md).

CPUs come in three tiers: Basic, Advance, and Ultimate. Higher tiers produce significantly more cycles per operation.

## Tier Compatibility

CPUs can be installed in [Servers](../machines/server.md) or [Computers](../machines/computer.md).

**Server:** Accepts all three tiers (Basic, Advance, Ultimate). Up to 9 CPUs per Server, and 9 Servers
per [Server Rack](../machines/server_rack.md).

**Computer:** Accepts Basic and Advance tiers only.

<Recipe id="logicore:processor_unit_basic" />

The entry-level processor. Capable of producing **1,000** [Cycles](../architecture/cycles.md) per operation. *Tier:
Basic.*

<Recipe id="logicore:processor_unit_advance" />

A mid-tier processor. Capable of producing **2,500** [Cycles](../architecture/cycles.md) per operation. *Tier: Advance.*

<Recipe id="logicore:processor_unit_ultimate" />

The pinnacle of processing technology. Capable of producing **5,000** [Cycles](../architecture/cycles.md) per operation.
*Tier: Ultimate.*
