# Show Everything Forge 1.12.2

Forge 1.12.2 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

Show items, blocks, fluids, and entities in chat with hover details. The command logic runs on the server, so clients do not need to install this mod.

## Requirements

- Minecraft `1.12.2`
- Forge `14.23.5.2847` or compatible 1.12.2 Forge build
- Java 8 for typical Minecraft 1.12.2 servers

## Installation

1. Build or download `showeverything-1.0.0.jar`.
2. Put the jar in the server `mods` folder.
3. Restart the server.

Client installation is optional and does not add extra features.

## Commands

All commands are available to normal players.

| Command | Description |
| --- | --- |
| `/show-item` | Shows the item in your main hand, or offhand if the main hand is empty. |
| `/show-block` | Shows the block you are looking at within 15 blocks. Falls back to your current position. |
| `/show-block <x> <y> <z>` | Shows the block at a specific position. |
| `/show-fluid` | Shows the fluid you are looking at within 15 blocks. Falls back to your current position. |
| `/show-fluid <x> <y> <z>` | Shows the fluid/block at a specific position. |
| `/show-entity` | Shows the entity you are looking at within 15 blocks. Falls back to yourself. |
| `/show-entity <selector>` | Shows a selected entity. |

Aliases without hyphens are also registered, such as `/showitem` and `/showblock`.

## Build

```bash
./gradlew build
```

The compiled mod jar is written to:

```text
build/libs/showeverything-1.0.0.jar
```

This repository uses ForgeGradle 2.3 and a Gradle 4.10.3 wrapper so CI can start on modern JDKs. Forge 1.12.2 runtime usage should still follow the usual Java 8 expectation.

## License

This port follows the repository license. The original mod is by MinersLab/WowStarWorld.
