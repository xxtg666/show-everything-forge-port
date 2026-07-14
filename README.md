# Show Everything Forge 1.7.10

Forge 1.7.10 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

Show items, blocks, fluids, and entities in chat with hover details. Command logic runs on the server, and the mod accepts clients that do not have Show Everything installed.

Clients that also install the mod get enhanced `/show-item` rendering for very large item NBT. Clients without it receive a shortened safe hover so oversized NBT cannot exceed Minecraft 1.7.10's chat packet limit.

## Requirements

- Minecraft `1.7.10`
- Forge `10.13.4.1614` (the recommended 1.7.10 build)
- Java 8

## Installation

1. Build or download `showeverything-forge-1.7.10-1.0.2.jar`.
2. Put the jar in the server `mods` folder.
3. Restart the server.

Client installation is optional. A server with this mod still accepts clients without it. Install it client-side only to see full hover data for unusually large item NBT.

## Commands

All commands are available to normal players.

| Command | Description |
| --- | --- |
| `/show-item` | Shows the item currently held by the player. |
| `/show-block` | Shows the block looked at within 15 blocks, falling back to the player's position. |
| `/show-block <x> <y> <z>` | Shows the block at specific coordinates. |
| `/show-fluid` | Shows the fluid looked at within 15 blocks, falling back to the player's position. |
| `/show-fluid <x> <y> <z>` | Shows the fluid at specific coordinates. |
| `/show-entity` | Shows the entity looked at within 15 blocks, falling back to the player. |
| `/show-entity <player-or-selector>` | Shows a selected entity. |

Aliases without hyphens are also available, such as `/showitem` and `/showblock`.

## Build

Use Java 8 and run:

```bash
./gradlew clean build
```

The compiled mod jar is written to:

```text
build/libs/showeverything-forge-1.7.10-1.0.2.jar
```

This branch uses the official Forge `1.7.10-10.13.4.1614-1.7.10` source distribution baseline with ForgeGradle 1.2 and Gradle 2.0.

## License

This port is maintained by xxtg666 and follows the repository license. The original mod is by MinersLab/WowStarWorld.
