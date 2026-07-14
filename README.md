# Show Everything Forge 1.12.2

Forge 1.12.2 port of [MinersLab/show-everything](https://github.com/MinersLab/show-everything).

Show items, blocks, fluids, and entities in chat with hover details. The command logic runs on the server, and the mod declares remote clients as optional, so vanilla/Forge clients without this mod can still join.

Clients that also install this mod get enhanced `/show-item` rendering for very large item NBT. Clients without the mod receive a safe shortened hover instead, so oversized NBT cannot kick players through Minecraft 1.12.2's chat packet string limit.

## Requirements

- Minecraft `1.12.2`
- Forge `14.23.5.2847` or compatible 1.12.2 Forge build
- Java 8 for typical Minecraft 1.12.2 servers

## Installation

1. Build or download `showeverything-forge-1.12.2-1.0.2.jar`.
2. Put the jar in the server `mods` folder.
3. Restart the server.

Client installation is optional. The server will not reject clients just because they do not have this mod installed. Install it client-side only if you want full hover display for very large item NBT.

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
build/libs/showeverything-forge-1.12.2-1.0.2.jar
```

This `main` branch targets Forge 1.12.2 and uses ForgeGradle 2.3. Forge 1.12.2 runtime usage should still follow the usual Java 8 expectation. Other supported Minecraft versions live on their matching branches; each branch has its own ForgeGradle setup and CI workflow.

## License

This port is maintained by xxtg666 and follows the repository license. The original mod is by MinersLab/WowStarWorld.
