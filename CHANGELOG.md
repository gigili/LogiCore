# Changelog

## 0.0.2

- Added `Recycler` enchantment
- Added `logicore:recycler_blacklist` tag to prevent `Recycler` enchantment from recycling certain blocks
- Added Cycle pick pickaxe

## 0.0.1

- Initial release
- Server rack block
- Computer block
- Data cable block
- Processor unit item
- Data center multiblock
- Custom textures and sounds
- Cycle generation
- Added custom tags to allow easier multiblock allowlisting
    - `logicore:valid_datacenter_wall_block` | what blocks can go into walls
    - `logicore:valid_datacenter_frame_block` | what blocks can go into frames (aka edges)
    - `logicore:valid_datacenter_inner_block` | what blocks can be placed inside a multiblock structure
        - `logicore:is_energy_generator` | tag already in a `valid_datacenter_inner_block` list
        - `logicore:is_energy_cable` | tag already in a `valid_datacenter_inner_block` list
- Added a compiler block which can convert one item into the other by using cycles
    - Added rendering of the selected item the compiler is generating
- Added a drone bay
- Added a simple drone for testing purposes
- Added a generator that converts fuels to FE
- Added a cloud interface block
    - It allows storing cycles in the "cloud" and sharing it with the team.
    - It can be used to transfer cycles from the cloud to machines as well
- Added wrench item
- Added battery blocks for energy storage
    - Small
    - Medium
    - Large
- Added AE2 integration via Cloud Interface block
- Added stack upgrade item for Compiler
- Added research station
