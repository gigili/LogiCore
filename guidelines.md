# LogiCore Development Guidelines

## Project Overview

LogiCore is a Minecraft 1.21.1 NeoForge mod centered around the concept of **Computation Networks**. The mod uses Forge
Energy (FE) to generate **Cycles**, which are then used to research and infinitely reproduce items.

## Core Concepts

- **Cycles**: A custom resource representing computational power.
- **Computation Network**: A network of blocks connected via Data Cables that distribute Cycles.
- **Modules**: Features are organized into self-contained modules for better maintainability.

## Project Structure

The project follows the standard Minecraft mod structure but with a modular approach in the Java source:

### Source Code (`src/main/java/dev/gacbl/logicore/`)

- `api/`: Shared interfaces (e.g., `ICycleConsumer`, `ICycleProvider`).
- `blocks/`: Organized by feature (e.g., `compiler/`, `generator/`).
    - Each sub-package typically contains:
        - `*Module`: Handles registration (`DeferredRegister`) and capabilities.
        - `*Block`: The block class.
        - `*BlockEntity`: The block entity class.
        - `ui/`: Contains `*Menu` and `*Screen`.
- `core/`: Core systems like `NetworkManager`, `ModDataMaps`, and `ModTags`.
- `data/`: Data generation and creative tab definitions.
- `items/`: Item-specific modules.
- `network/`: Packet handling.

### Resources (`src/main/resources/`)

- `assets/logicore/`: Models, textures, and blockstates.
- `data/logicore/`: Recipes, loot tables, tags, and data maps.

## Coding Practices

### 1. Modular Registration

Every new feature should have a `*Module` class.

- Use `DeferredRegister` for all registry objects.
- Centralize `register` methods and call them from the main `LogiCore` constructor.
- Handle `RegisterCapabilitiesEvent` within the module.

### 2. Computation Network (Cycles)

- Use `ICycleProvider` and `ICycleConsumer` interfaces for blocks interacting with the network.
- Integration with the network is managed by `NetworkManager`.
- Data Cables facilitate the connection between providers and consumers.

### 3. Multiblock Structures

- Use `AbstractSealedController` and `MultiblockValidator` for room-based multiblocks (like the Datacenter).
- Define valid blocks using tags in `ModTags`.

### 4. Data Maps

- Item-specific Cycle values should be defined via NeoForge Data Maps.
- The `item_cycles` data map is defined in `ModDataMaps`.

### 5. UI and Menus

- Screens and Menus should be kept in a `ui` sub-package within the feature package.
- Register menus in the `*Module` and screens in `LogiCore.ClientModEvents`.

### 6. Naming Conventions

- Registry Names: `snake_case` (e.g., `datacenter_controller`).
- Java Classes: `PascalCase` (e.g., `GeneratorBlockEntity`).
- Block Entities: Suffix with `BlockEntity` or `BE`.
- Modules: Suffix with `Module`.

## Best Practices

- **Server/Client Separation**: Always check `level.isClientSide` before performing logic in Block Entities.
- **Capabilities**: Use NeoForge's capability system for Energy and Item handling.
- **Performance**: Use dirty flags (e.g., `cacheDirty`) for expensive operations like multiblock validation or network
  scans.
- **Data Generation**: Prefer using data generators for recipes, models, and tags to ensure consistency.

## Expanding the Mod

When adding a new feature:

1. Create a new sub-package in `dev.gacbl.logicore.blocks` or `dev.gacbl.logicore.items` or any other appropriate
   package depending on the features use case.
2. Implement the `*Module` and register it in `LogiCore`.
3. Add required assets and data files (or generators).
4. Update `ModTags` or `ModDataMaps` if new integration is needed.
