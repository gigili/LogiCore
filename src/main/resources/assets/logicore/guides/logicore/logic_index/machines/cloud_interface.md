---
navigation:
  title: "Cloud Interface"
  icon: "logicore:cloud_interface"
  parent: logicore:machines_category.md
item_ids:
  - logicore:cloud_interface
---

# Cloud Interface

The *Cloud Interface* is a gateway to your digital cycle storage.

It allows you to upload generated cycles to a global "Cloud" account. These cycles are stored in the world data, meaning
they are accessible from any other Cloud Interface, anywhere and in any dimension.

Provides access to the global Cycle Network.

<Recipe id="logicore:cloud_interface" />

## I/O Configuration

The block has specific directional logic:

- **Back Input:** Connect a Generator or Network Cable here to <Color id="dark_green">UPLOAD</Color> cycles to the
  cloud.
- **Output Sides:** Any other side will <Color id="dark_red">DOWNLOAD</Color> cycles from the cloud to power adjacent
  machines.

## Team Storage

Cloud storage is automatically keyed to your account.

If you join a **Team** (via FTB Teams or Vanilla), your interface will instantly switch to the Team's shared storage
pool.

This allows all party members to contribute to and withdraw from the same energy source.

## Offline Access

Unlike personal inventory, the Cloud Interface works via **Global Saved Data**.

This means automation will continue to run and process cycles even if the owner is offline, provided the chunk is
loaded.
