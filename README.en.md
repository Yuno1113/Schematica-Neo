# Schematica-Neo

[中文说明](README.md)

**Schematica-Neo** is a Schematica extension and modernization project for **Minecraft 1.7.10**. It keeps the classic schematic loading, rendering, material-list, and printing features, while adding schematic editing, structure-file conversion, more flexible visibility controls, and compatibility support for **ManaMetalMod**.

The project is intended to improve the workflow for viewing, modifying, converting, and building large schematic-based structures.

Some of the code in this mod was generated, refactored, debugged, and organized with the assistance of AI programming tools. The project author is responsible for the final implementation, feature decisions, testing, and release contents. AI tools are used only as development assistance, and the project may still contain bugs or untested edge cases. Bug reports and feedback are welcome through Issues.

## Features

### Schematic Loading and Controls

- Load `.schematic` files.
- Move, rotate, flip, and unload schematics.
- Save schematic coordinates and transformation states.
- Use several schematic visibility modes:
  - Show all
  - Layer view
  - Selection view
  - Single-block view, with inventory block selection and editable metadata
- Hide or show the current schematic.

### Schematic Editing

The unified schematic editor provides the following modes when editing is enabled:

- **Selection**
  - Create a selection in the world or in the loaded schematic.
  - Use `Ctrl + Mouse Wheel` to resize the selection.
  - Use `Alt + Mouse Wheel` to move the selection.
  - Use `Alt + Middle Mouse Button` to reset the selection center near the player.
  - Enable two-button point selection to set the two corners with the left and right mouse buttons.

- **Copy**
  - Copy blocks from the world or the schematic into a temporary buffer.

- **Cut**
  - Copy the selected blocks into the buffer and remove them from the currently loaded schematic.

- **Paste**
  - Preview the buffered blocks and their bounds before merging them.
  - Use `Alt + Mouse Wheel` to move the paste position along the player's view direction.
  - Preserve existing non-air schematic blocks at overlapping positions when merging.
  - Automatically expand the schematic when the paste range exceeds its current bounds.

- **Replace Blocks**
  - Select the replacement block from the player's inventory.
  - Edit the target metadata.
  - Replace matching blocks individually, within a selection, or throughout the schematic.

- **Rotate Block Metadata**
  - Apply the operation to an individual block, a selection, or the entire schematic.
  - Increase or decrease the metadata of the target block.

- **Move Schematic**
  - Use `Alt + Mouse Wheel` to move the schematic along the player's primary view direction.
  - Use `Alt + Middle Mouse Button` to reset the schematic near the player.

### Saving

Saving is available in two forms:

- **Save Selection**
  - Save the current selection as a new schematic file.
  - Choose whether blocks are read from the actual world or the currently loaded schematic.

- **Save Schematic**
  - Save the complete schematic after copy, cut, paste, replacement, or metadata edits.

## Structure File Import

Schematica-Neo can convert some structure files from newer Minecraft versions into `.schematic` files that can be used in Minecraft 1.7.10.

Supported formats currently include:

- `.nbt` structure files from vanilla Minecraft and Create
- `.litematic` files

This conversion feature is primarily designed for **ManaMetalMod** environments. Using the block mapping tables included in the project, it maps block states from Minecraft 1.13 and newer to corresponding Minecraft 1.7.10 or ManaMetalMod blocks.

When no mapping is available for a block:

- A numbered marker block is used as a replacement.
- A reference list is generated to match marker numbers with missing blocks.
- The reference list can be saved as a local text file.
- In single-player, a reference book can be generated and placed in the player's inventory.

Because marker blocks are provided by Schematica-Neo, they may not render correctly on a server without this mod. Using structure conversion in single-player or on a server with Schematica-Neo installed is recommended.

## Material List

- Count blocks in the schematic and available materials in the player's inventory.
- Distinguish placed amounts, total amounts, and remaining required materials.
- Sort by name or quantity.
- Switch between plain quantities and `stacks:items` display.
- Export the material list to a local text file.
- Include total quantities, stack counts, and estimated storage requirements in exports.

## Assisted Printing

- Automatically select required materials from the player's inventory.
- Place blocks by layer or by distance priority.
- Use multiple printing speed levels.
- The default speed is `Fast`.
- Experimental high-speed levels are available in single-player.
- Multiplayer printing is limited to `3x`.
- Handle placement direction and click positions for slabs, stairs, pistons, torches, doors, hoppers, and other blocks with special placement rules.

## ManaMetalMod Compatibility

The project includes compatibility features for **ManaMetalMod**, including:

- Mapping newer-version blocks to ManaMetalMod blocks. See the [ManaMetalMod block mapping table](src/main/resources/assets/schematicaneo/mmm_blocks_reference.txt) and the [vanilla block mapping table](src/main/resources/assets/schematicaneo/vanilla_blocks_reference.txt).
- Isolating preview errors caused by special ManaMetalMod blocks.
- Special metadata handling for ManaMetalMod slabs whose rules differ from vanilla slabs.
- Extended metadata reading, editing, and saving support.

These compatibility rules only apply to the `manametalmod` registry namespace and do not change the metadata rules for vanilla slabs.

## Controls

- `Shift + Mouse Wheel`: switch editing modes.
- `Ctrl + Mouse Wheel`: resize the selection in selection mode.
- `Alt + Mouse Wheel`: move the selection, paste preview, or schematic depending on the current mode.
- `Alt + Middle Mouse Button`: reset the selection center or schematic position.
- `Enter`: execute the corresponding operation in copy, cut, or paste mode.
- Editing may intercept some vanilla mouse actions. Disable the editing switch when editing is not needed.

Key bindings can be changed in Minecraft's controls settings.

## Requirements

- Minecraft `1.7.10`
- Minecraft Forge `10.13.4.1614`
- Java 8
- [LunatriusCore](https://github.com/GTNewHorizons/LunatriusCore)
- Works in single-player and multiplayer
- Additional compatibility with ManaMetalMod

## Project Status

This project is still under development and testing. Schematic editing, special block rendering, complex Tile Entities, mod-specific placement logic, and large-schematic performance may still have untested edge cases.

When reporting an issue, please include:

- Minecraft, Forge, and Schematica-Neo versions
- Versions of relevant installed mods
- Steps to reproduce the issue
- A crash report or `latest.log`
- The affected block registry name and metadata
- A schematic file that reproduces the issue, if possible

## Origin and License

Schematica-Neo is based on [GTNewHorizons/Schematica](https://github.com/GTNewHorizons/Schematica) and retains the upstream project's MIT license and original copyright notices. The upstream license text is available in the `LICENSE` file at the repository root.

New and modified parts of this project are also released under the MIT License. When using, modifying, or redistributing this project, please comply with the licenses of this repository and its third-party components.

The project uses [LunatriusCore](https://github.com/GTNewHorizons/LunatriusCore) as a dependency. ManaMetalMod is one of the compatibility targets of this project; Schematica-Neo is not officially affiliated with or endorsed by the authors or maintainers of ManaMetalMod.
