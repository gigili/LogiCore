# LogiCore changelog

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
    - logicore:valid_datacenter_wall_block | what blocks can go into walls
    - logicore:valid_datacenter_frame_block | what blocks can go into frames (aka edges)
    - logicore:valid_datacenter_inner_block | what blocks can be placed inside a multiblock structure
        - logicore:is_energy_generator | tag already in a valid_datacenter_inner_block list
        - logicore:is_energy_cable | tag already in a valid_datacenter_inner_block list  
